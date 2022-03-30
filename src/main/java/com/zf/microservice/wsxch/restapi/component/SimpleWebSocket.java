package com.zf.microservice.wsxch.restapi.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zf.microservice.wsxch.restapi.object.constant.*;
import com.zf.microservice.wsxch.restapi.object.entity.WsxchRabbitMessage;
import com.zf.microservice.wsxch.restapi.procedure.SimpleWebSocketManager;
import com.zf.microservice.wsxch.restapi.producer.RabbitmqProducer;
import com.zf.microservice.wsxch.restapi.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@ServerEndpoint(
        value = "/wsxch/{wsid}"
)
@Component
public class SimpleWebSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleWebSocket.class);

    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    public String getWsid() {
        return this.wsid;
    }

    /**
     * 发送点对点消息
     *
     * @param destination
     * @param message
     */
    public static Integer send(String destination, String message) {
        Integer code;

        SimpleWebSocket destinationInstance = SimpleWebSocketManager.getInstance(destination);
        if (destinationInstance != null) {
            destinationInstance.activeTime = System.currentTimeMillis();
            Boolean isSuccess = destinationInstance.sendText(message);
            code = isSuccess ? CodeConst.SUCCESS : CodeConst.FAILURE;
        } else {
            code = CodeConst.FAILURE;
        }

        return code;
    }

    /**
     * 发送广播消息
     *
     * @param ticket
     * @param message
     */
    public static Integer broadcast(String ticket, String category, String message) {
        Integer code = CodeConst.DEFAULT;
        Map<String, SimpleWebSocket> instanceQueue = SimpleWebSocketManager.getInstanceQueue();
        Set<String> instances = instanceQueue.keySet();
        if (!CollectionUtils.isEmpty(instanceQueue)) {
            instances.forEach(instance -> instanceQueue.get(instance).sendText(ticket, message));
            code = CodeConst.SUCCESS;
        }
        return code;
    }

    /**
     * onOpen method.
     *
     * @param session
     * @param wsid
     * @throws IOException
     */
    @OnOpen
    public void onOpen(
            Session session,
            @PathParam("wsid") String wsid
    ) {
        if (wsid.equalsIgnoreCase(WS_TICKET_CREATE)) {
            wsid = UuidUtil.getUuid();
        }
        //todo delete log
//        System.out.println("ON_OPEN: WSID -> " + wsid);
        LOGGER.info("ON_OPEN: WSID -> {}", wsid);
        JSONObject destinationJson = new JSONObject();
        destinationJson.put(WsxchJsonKeyConst.WSID, wsid);

        this.wsid = wsid;
        this.session = session;
        this.isOpen = true;
        activeTime = System.currentTimeMillis();

        sendText(WS_TICKET_WSID, wsid);

        SimpleWebSocketManager.addInstance(this);
        SimpleWebSocket instance = SimpleWebSocketManager.getInstance(wsid);
        LOGGER.info("instance {}", JSON.toJSONString(instance));
        INSTANCE.simpleRedisRepository.cacheObjectByArgs(WsxchCacheKeyConst.WSXCH, destinationJson.toJSONString(), GlobalConst.SECONDS_IN_DAY, wsid);
        JSONObject queueNameJson = INSTANCE.simpleRedisRepository.fetchObject(WsxchCacheKeyConst.WSXCH_QUEUE_NAME, JSONObject.class, WsxchCacheKeyConst.DIRECT);
        if (queueNameJson == null) {
            queueNameJson = new JSONObject();
        }
        queueNameJson.put(wsid, SimpleWebSocketCore.getDirectQueueName());
        INSTANCE.simpleRedisRepository.cacheObjectByArgs(WsxchCacheKeyConst.WSXCH_QUEUE_NAME, queueNameJson.toJSONString(), null, WsxchCacheKeyConst.DIRECT);
    }

    /**
     * onClose method.
     */
    @OnClose
    public void onClose() {
        try {
            if (wsid != null) {
                //todo delete log
//            System.out.println("ON_CLOSE: WSID -> " + wsid + " HAS BEEN CLOSED.");
                LOGGER.info("ON_CLOSE: WSID -> {} HAS BEEN CLOSED.", wsid);
                Map<String, Integer> serverCounter = SimpleWebSocketManager.getServerCounter();
                if (serverCounter.containsKey(wsid)) {
                    JSONObject serverJson = INSTANCE.simpleRedisRepository.fetchObject(WsxchCacheKeyConst.SERVER_WSID, JSONObject.class, WsxchCacheKeyConst.SERVER);
                    serverJson.remove(wsid);
                    INSTANCE.simpleRedisRepository.cacheObjectByArgs(WsxchCacheKeyConst.SERVER_WSID, serverJson.toJSONString(), null, WsxchCacheKeyConst.SERVER);
                }
                SimpleWebSocketManager.removeInstance(wsid);
                JSONObject queueNameJson = INSTANCE.simpleRedisRepository.fetchObject(WsxchCacheKeyConst.WSXCH_QUEUE_NAME, JSONObject.class, WsxchCacheKeyConst.DIRECT);
                queueNameJson.remove(wsid);
                INSTANCE.simpleRedisRepository.cacheObjectByArgs(WsxchCacheKeyConst.WSXCH_QUEUE_NAME, queueNameJson.toJSONString(), null, WsxchCacheKeyConst.DIRECT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
    }

    /**
     * onMessage method.
     *
     * @param dataStr
     * @throws IOException
     */
    @OnMessage
    public void onMessage(String dataStr) {
        LOGGER.info("ON_MESSAGE: WSID -> {} send message.", wsid);
        try {
            JSONObject dataJson = JSON.parseObject(dataStr);
            String ticket = dataJson.getString(WsxchJsonKeyConst.TICKET);
            switch (ticket) {
                case WS_TICKET_CLIENT_SIGNIN: {
                    this.type = WS_TYPE_CLIENT;
                    this.isOpenChat = true;
                    JSONObject openJson = new JSONObject();
                    JSONObject clientJson = dataJson.getJSONObject(WsxchJsonKeyConst.MESSAGE);
                    String clientWsid = clientJson.getString(WsxchJsonKeyConst.WSID);

                    INSTANCE.simpleRedisRepository.cacheObjectByArgs(WsxchCacheKeyConst.WSXCH, clientJson.toJSONString(), GlobalConst.SECONDS_IN_DAY, clientWsid);

                    String serverWsid = dataJson.getString(WsxchJsonKeyConst.SERVER_WSID);
                    JSONObject serverJson = INSTANCE.simpleRedisRepository.fetchObject(WsxchCacheKeyConst.SERVER_WSID, JSONObject.class, WsxchCacheKeyConst.SERVER);
                    if (!Objects.isNull(serverJson)) {
                        if (!serverJson.containsKey(serverWsid)) {
                            serverWsid = SimpleWebSocketManager.getLeastConnectionServer(serverJson);
                            if (serverWsid != null) {
                                INSTANCE.simpleRedisRepository.cacheObjectByArgs(WsxchCacheKeyConst.SERVER_WSID, serverJson.toJSONString(), GlobalConst.SECONDS_IN_DAY, WsxchCacheKeyConst.SERVER);
                            }
                        }
                    } else {
                        serverWsid = null;
                    }
                    if (serverWsid == null) {
                        sendText(WS_TICKET_NO_SERVER, EMPTY_MESSAGE);
                    } else {
                        JSONArray serverClientArray = INSTANCE.simpleRedisRepository.fetchWsxchArray(
                                WsxchCacheKeyConst.WSXCH_CHAT_CLIENTS,
                                serverWsid,
                                serverWsid
                        );
                        if (CollectionUtils.isEmpty(serverClientArray)) serverClientArray = new JSONArray();
                        AtomicBoolean flag = new AtomicBoolean(true);
                        serverClientArray.removeIf((client) -> StringUtils.isEmpty(((JSONObject)client).getString(WsxchJsonKeyConst.WSID)));
                        serverClientArray.forEach(client -> {
                            if (((JSONObject) client).getString(WsxchJsonKeyConst.WSID).equals(clientWsid)) {
                                flag.set(false);
                            };
                        });
                        if (flag.get()) {
                            serverClientArray.add(clientJson);
                        }
                        // cache server matched client wsid
                        INSTANCE.simpleRedisRepository.cacheObjectByArgs(
                                WsxchCacheKeyConst.WSXCH_CHAT_CLIENTS,
                                serverClientArray.toJSONString(),
                                GlobalConst.SECONDS_IN_DAY,
                                serverWsid,
                                serverWsid
                        );

                        JSONArray historyArray = INSTANCE.simpleRedisRepository.fetchWsxchArray(
                                WsxchCacheKeyConst.WSXCH_CHAT_HISTORY,
                                serverWsid,
                                clientWsid
                        );

                        JSONObject server = INSTANCE.simpleRedisRepository.fetchJsonObject(
                                WsxchCacheKeyConst.WSXCH, serverWsid
                        );
                        Long agentId = server.getLong(WsxchJsonKeyConst.AGENT_ID);

                        dataJson.put(Constants.AGENT_ID, agentId);

                        boolean hasKey = CollectionUtils.isEmpty(historyArray);

                        if (hasKey) {
                            JSONObject initJson = new JSONObject();
                            initJson.put(WsxchJsonKeyConst.TYPE, CHAT_MESSAGE_TYPE_ROBOT);
                            initJson.put(WsxchJsonKeyConst.SEND_TIME, System.currentTimeMillis());
                            initJson.put(WsxchJsonKeyConst.MESSAGE, "Create Chat Channel Success. ");
                            historyArray.add(initJson);
                            INSTANCE.simpleRedisRepository.cacheObjectByArgs(
                                    WsxchCacheKeyConst.WSXCH_CHAT_HISTORY,
                                    historyArray.toJSONString(),
                                    GlobalConst.SECONDS_IN_DAY,
                                    serverWsid,
                                    clientWsid
                            );
                        }

                        JSONObject messageChatJson = new JSONObject();
                        messageChatJson.put(WsxchJsonKeyConst.DESTINATION, serverWsid);
                        messageChatJson.put(WsxchJsonKeyConst.AGENT_ID, agentId);
                        messageChatJson.put(WsxchJsonKeyConst.HISTORY, historyArray);
                        JSONObject messageJson = new JSONObject();
                        messageJson.put(WsxchJsonKeyConst.TICKET, WS_TICKET_CHAT_START);
                        messageJson.put(WsxchJsonKeyConst.MESSAGE, messageChatJson);
                        sendMessage(clientWsid, messageJson.toJSONString());

                        messageChatJson.put(WsxchJsonKeyConst.DESTINATION, clientJson);
                        messageJson.put(WsxchJsonKeyConst.MESSAGE, messageChatJson);
                        sendMessage(serverWsid, messageJson.toJSONString());
                    }
                    INSTANCE.rabbitmqProducer.sendWithQueueName(
                            MessageConversionUtil.convertInstantChatStatistics(dataJson),
                            INSTANT_MESSAGING_QUEUE
                    );
                    break;
                }
                case WS_TICKET_SERVER_SIGNIN: {
                    this.type = WS_TYPE_SERVER;

                    JSONObject messageJson = dataJson.getJSONObject(WsxchJsonKeyConst.MESSAGE);
                    String serverWsid = messageJson.getString(WsxchJsonKeyConst.WSID);

                    INSTANCE.simpleRedisRepository.cacheObjectByKeys(
                            WsxchCacheKeyConst.WSXCH, messageJson,
                            WsxchJsonKeyConst.WSID
                    );

                    SimpleWebSocketManager.serverSignin(serverWsid);

                    JSONArray clientArray = INSTANCE.simpleRedisRepository.fetchWsxchArray(
                            WsxchCacheKeyConst.WSXCH_CHAT_CLIENTS,
                            serverWsid,
                            serverWsid
                    );
                    serverSignIn(serverWsid);
                    if (CollectionUtils.isEmpty(clientArray)) clientArray = new JSONArray();
                    sendText(WS_TICKET_CHAT_SESSIONS, clientArray.toJSONString());
                    break;
                }
                case WS_TICKET_CHAT_CLOSE: {
                    String destinationWsid = dataJson.getString(WsxchJsonKeyConst.DESTINATION);
                    JSONObject messageJson = dataJson.getJSONObject(WsxchJsonKeyConst.MESSAGE);

                    if (destinationWsid.equals(messageJson.getString(WsxchJsonKeyConst.CLIENT_WSID))) {
                        String serverWsid = messageJson.getString(WsxchJsonKeyConst.SERVER_WSID);

                        JSONArray serverArray = INSTANCE.simpleRedisRepository.fetchWsxchArray(
                                WsxchCacheKeyConst.WSXCH_CHAT_CLIENTS,
                                serverWsid,
                                serverWsid
                        );
                        serverArray.removeIf(client -> ((JSONObject) client).getString(WsxchJsonKeyConst.WSID)
                                .equals(destinationWsid));
                        INSTANCE.simpleRedisRepository.cacheObjectByArgs(
                                WsxchCacheKeyConst.WSXCH_CHAT_CLIENTS,
                                serverArray.toJSONString(),
                                GlobalConst.SECONDS_IN_DAY,
                                serverWsid,
                                serverWsid
                        );
                    }
                    sendMessage(destinationWsid, dataJson.toJSONString());
                    break;
                }
                case WS_TICKET_CHAT_MESSAGE: {
                    String destinationWsid = dataJson.getString(WsxchJsonKeyConst.DESTINATION);
                    String serverWsid, clientWsid;
                    if (this.type == WS_TYPE_CLIENT) {
                        clientWsid = this.wsid;
                        serverWsid = destinationWsid;
                    } else {
                        clientWsid = destinationWsid;
                        serverWsid = this.wsid;
                    }
                    JSONObject messageJson = dataJson.getJSONObject(WsxchJsonKeyConst.MESSAGE);
                    messageJson.put(WsxchJsonKeyConst.SEND_TIME, System.currentTimeMillis());

                    JSONArray historyArray = INSTANCE.simpleRedisRepository.fetchWsxchArray(
                            WsxchCacheKeyConst.WSXCH_CHAT_HISTORY,
                            serverWsid,
                            clientWsid
                    );
                    historyArray.add(messageJson);
                    INSTANCE.simpleRedisRepository.cacheObjectByArgs(
                            WsxchCacheKeyConst.WSXCH_CHAT_HISTORY,
                            historyArray.toJSONString(),
                            GlobalConst.SECONDS_IN_DAY,
                            serverWsid,
                            clientWsid
                    );
                    INSTANCE.rabbitmqProducer.sendWithQueueName(
                            MessageConversionUtil.convertInstantMessaging(dataJson),
                            INSTANT_MESSAGING_QUEUE
                    );
                    sendMessage(destinationWsid, dataJson.toJSONString());
                    break;
                }
                case WS_TICKET_CHAT_HISTORY: {
                    String serverWsid = dataJson.getString(WsxchJsonKeyConst.SERVER_WSID);
                    String clientWsid = dataJson.getString(WsxchJsonKeyConst.CLIENT_WSID);

                    JSONArray historyArray = INSTANCE.simpleRedisRepository.fetchWsxchArray(
                            WsxchCacheKeyConst.WSXCH_CHAT_HISTORY,
                            serverWsid,
                            clientWsid
                    );

                    sendText(ticket, historyArray.toJSONString());
                    break;
                }
                case WS_TICKET_CHAT_TRANSFORM: {
                    String serverWsid = dataJson.getString(WsxchJsonKeyConst.SERVER_WSID);
                    JSONObject serverWsidJson = INSTANCE.simpleRedisRepository.fetchObject(WsxchCacheKeyConst.SERVER_WSID, JSONObject.class, WsxchCacheKeyConst.SERVER);
                    JSONArray onlineServer = new JSONArray();
                    if (!Objects.isNull(serverWsidJson)) {
                        Set<String> wsidSet = serverWsidJson.keySet();
                        for (String wsid : wsidSet) {
                            if (serverWsid.equals(wsid) || StringUtils.isEmpty(wsid)) {
                                continue;
                            }
                            JSONObject serverJson = INSTANCE.simpleRedisRepository.fetchObject(WsxchCacheKeyConst.WSXCH, JSONObject.class, wsid);
                            if (!Objects.isNull(serverJson)) {
                                onlineServer.add(serverJson);
                            }
                        }
                    }
                    sendText(ticket, onlineServer.toJSONString());
                    break;
                }
                case WS_TICKET_CHAT_TRANSFORM_ACTIVE: {
                    String serverWsid = dataJson.getString(WsxchJsonKeyConst.SERVER_WSID);
                    String clientWsid = dataJson.getString(WsxchJsonKeyConst.CLIENT_WSID);
                    String transformWsid = dataJson.getString(WsxchJsonKeyConst.TRANSFORM_WSID);

                    JSONObject clientJson = INSTANCE.simpleRedisRepository.fetchObject(WsxchCacheKeyConst.WSXCH, JSONObject.class, clientWsid);

                    if (clientJson.isEmpty()) {
                        System.out.println("Transform active failed, client not in redis.");
                        return;
                    }
                    JSONArray transformClientArray = INSTANCE.simpleRedisRepository.fetchWsxchArray(
                            WsxchCacheKeyConst.WSXCH_CHAT_CLIENTS,
                            transformWsid,
                            transformWsid
                    );
                    transformClientArray.add(clientJson);
                    INSTANCE.simpleRedisRepository.cacheObjectByArgs(
                            WsxchCacheKeyConst.WSXCH_CHAT_CLIENTS,
                            transformClientArray.toJSONString(),
                            GlobalConst.SECONDS_IN_DAY,
                            transformWsid,
                            transformWsid
                    );

                    JSONArray serverClientArray = INSTANCE.simpleRedisRepository.fetchWsxchArray(
                            WsxchCacheKeyConst.WSXCH_CHAT_CLIENTS,
                            serverWsid,
                            serverWsid
                    );

                    serverClientArray.removeIf(client -> ((JSONObject) client).getString(WsxchJsonKeyConst.WSID).equals(clientWsid));
                    INSTANCE.simpleRedisRepository.cacheObjectByArgs(
                            WsxchCacheKeyConst.WSXCH_CHAT_CLIENTS,
                            serverClientArray.toJSONString(),
                            GlobalConst.SECONDS_IN_DAY,
                            serverWsid,
                            serverWsid
                    );

                    JSONArray historyArray = INSTANCE.simpleRedisRepository.fetchWsxchArray(
                            WsxchCacheKeyConst.WSXCH_CHAT_HISTORY,
                            serverWsid,
                            clientWsid
                    );

                    JSONArray transformHistoryArray = INSTANCE.simpleRedisRepository.fetchWsxchArray(
                            WsxchCacheKeyConst.WSXCH_CHAT_HISTORY,
                            transformWsid,
                            clientWsid
                    );
                    INSTANCE.simpleRedisRepository.deleteCacheKeyByArgs(WsxchCacheKeyConst.WSXCH_CHAT_HISTORY, serverWsid, clientWsid);

                    transformHistoryArray.addAll(historyArray);

                    INSTANCE.simpleRedisRepository.cacheObjectByArgs(
                            WsxchCacheKeyConst.WSXCH_CHAT_HISTORY,
                            transformHistoryArray.toJSONString(),
                            GlobalConst.SECONDS_IN_DAY,
                            transformWsid,
                            clientWsid
                    );

                    sendText(WS_TICKET_CHAT_SESSIONS, serverClientArray.toJSONString());

                    JSONObject transformMessageJson = new JSONObject();
                    transformMessageJson.put(WsxchJsonKeyConst.TICKET, WS_TICKET_CHAT_START);
                    transformMessageJson.put(WsxchJsonKeyConst.MESSAGE, new JSONObject() {{
                        put(WsxchJsonKeyConst.DESTINATION, clientJson);
                    }});
                    sendMessage(transformWsid, transformMessageJson.toJSONString());

                    JSONObject clientMessageJson = new JSONObject() {{
                        put(WsxchJsonKeyConst.MESSAGE, new JSONObject() {{
                            put(WsxchJsonKeyConst.SERVER_WSID, transformWsid);
                        }});
                        put(WsxchJsonKeyConst.TICKET, ticket);
                    }};
                    sendMessage(clientWsid, clientMessageJson.toJSONString());
                    break;
                }
                case WS_TICKET_CHAT_RESPONSE: {
                    dataJson.put(Constants.RESPONSE_TIME, System.currentTimeMillis());
                    INSTANCE.rabbitmqProducer.sendWithQueueName(dataJson.toJSONString(), INSTANT_MESSAGING_QUEUE);
                    break;
                }
                default: {
                    String destinationWsid = dataJson.getString(WsxchJsonKeyConst.DESTINATION);
                    sendMessage(destinationWsid, dataJson.toJSONString());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * onError method.
     *
     * @param error
     * @throws IOException
     */
    @OnError
    public void onError(Throwable error) {
        if (wsid != null) {
            //todo delete log
//            System.out.println("ON_ERROR: " + wsid);
            LOGGER.info("ON_ERROR: {}", wsid);
            error.printStackTrace();
            onClose();
        }
    }

    public Boolean sendText(String messageData) {
        try {
            LOGGER.info("SEND_TEXT: WSID -> {} send text.", wsid);
            synchronized (this.session) {
                this.session.getBasicRemote().sendText(messageData);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            onError(e);
            return false;
        }
    }

    /**
     * send method.
     *
     * @param ticket
     * @param message
     * @return
     */
    private Boolean sendText(String ticket, String message) {
        try {
            this.activeTime = System.currentTimeMillis();
            JSONObject messageJson = new JSONObject();
            messageJson.put(WsxchJsonKeyConst.TICKET, ticket);
            messageJson.put(WsxchJsonKeyConst.MESSAGE, message);
            this.session.getBasicRemote().sendText(messageJson.toJSONString());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            onError(e);
            return false;
        }
    }

    private void close() {
        isOpen = false;
        GC.close(this.session);
    }

    public static boolean sendMessage(String destinationWsid, String messageData) {
        //todo delete log
        LOGGER.info("sendMessage >>>>>>>>>>>>>>>>>> {}, message {}", destinationWsid, messageData);
        Boolean result = false;
        try {
            if (StringUtils.isBlank(destinationWsid)) {
                return result;
            }
            SimpleWebSocket clientWebSocket = SimpleWebSocketManager.getInstance(destinationWsid);
            if (clientWebSocket != null) {
                clientWebSocket.activeTime = System.currentTimeMillis();
                result = clientWebSocket.sendText(messageData);
                LOGGER.info("send text is success {}: WSID -> {}.", result, destinationWsid);
                return result;
            }
            LOGGER.info("[Send Message]No socket was found. WSID:{}", destinationWsid);
            JSONObject wsxchJson = INSTANCE.simpleRedisRepository.fetchObject(WsxchCacheKeyConst.WSXCH_QUEUE_NAME, JSONObject.class, WsxchCacheKeyConst.DIRECT);
            if (wsxchJson.containsKey(destinationWsid)) {
                String queueName = wsxchJson.getString(destinationWsid);
                WsxchRabbitMessage rabbitMessage = new WsxchRabbitMessage(destinationWsid, messageData);
                INSTANCE.rabbitmqProducer.sendWithQueueName(rabbitMessage, queueName);
                result = true;
            } else {
                LOGGER.error("[Send Message]Failed to send message. WSID:{}", destinationWsid);
                LOGGER.info("wsxchJson >>>>>>>>>>>>>>> {}", wsxchJson);
            }
        } catch (Exception e) {
            LOGGER.error("[Send Message]Failed to send message. WSID:{}", destinationWsid, e);
        }
        return result;
    }

    private void serverSignIn(String serverWsid) {
        JSONObject serversJson = INSTANCE.simpleRedisRepository.fetchObject(WsxchCacheKeyConst.SERVER_WSID, JSONObject.class, WsxchCacheKeyConst.SERVER);
        if (Objects.isNull(serversJson))  serversJson = new JSONObject();
        if (!serversJson.containsKey(serverWsid)) {
            serversJson.put(serverWsid, 0);
            INSTANCE.simpleRedisRepository.cacheObjectByArgs(WsxchCacheKeyConst.SERVER_WSID, serversJson.toJSONString(), null, WsxchCacheKeyConst.SERVER);
            JSONObject serversHistoryJson = INSTANCE.simpleRedisRepository.fetchObject(WsxchCacheKeyConst.SERVER_WSID_HISTORY, JSONObject.class, WsxchCacheKeyConst.SERVER);
            if (Objects.isNull(serversHistoryJson)) {
                serversHistoryJson = new JSONObject();
            }
            String date = serversHistoryJson.getString(WsxchJsonKeyConst.DATE);
            String dateStr = DateUtil.dateToStr(DateUtil.getCurrentDate());
            if (StringUtils.isBlank(date)) {
                serversHistoryJson.put(WsxchJsonKeyConst.DATE, dateStr);
                serversHistoryJson.put(serverWsid, 0);
            } else {
                if (date.equals(dateStr)) {
                    serversHistoryJson.put(serverWsid, 0);
                } else {
                    serversHistoryJson.clear();
                    serversHistoryJson.put(WsxchJsonKeyConst.DATE, dateStr);
                    serversHistoryJson.put(serverWsid, 0);
                }
            }
            INSTANCE.simpleRedisRepository.cacheObjectByArgs(WsxchCacheKeyConst.SERVER_WSID_HISTORY, serversHistoryJson.toJSONString(), null, WsxchCacheKeyConst.SERVER);
        }
    }

    public Long getActiveTime() {
        return this.activeTime;
    }


    @Autowired
    private WsxchRedisRepository simpleRedisRepository;

    @Autowired
    private RabbitmqProducer rabbitmqProducer;

    private String wsid;
    private Integer type;
    private Session session;
    private Long activeTime = null;
    private Boolean isOpen = false;
    private Boolean isOpenChat = false;

    public static final int WS_TYPE_SERVER = 1;
    public static final int WS_TYPE_CLIENT = 2;

    public static final String WS_TICKET_CREATE = "CREATE";
    public static final String WS_TICKET_WSID = "WSID";
    public static final String WS_TICKET_CLIENT_SIGNIN = "CLIENT_SIGNIN";
    public static final String WS_TICKET_SERVER_SIGNIN = "SERVER_SIGNIN";
    public static final String WS_TICKET_NO_SERVER = "NO_SERVER";
    public static final String WS_TICKET_CHAT_START = "CHAT_START";
    public static final String WS_TICKET_CHAT_STOP = "CHAT_STOP";
    private static final String WS_TICKET_CHAT_CLOSE = "CHAT_CLOSE";
    public static final String WS_TICKET_CHAT_MESSAGE = "CHAT_MESSAGE";
    public static final String WS_TICKET_CHAT_SESSIONS = "CHAT_SESSIONS";
    public static final String WS_TICKET_CHAT_HISTORY = "CHAT_HISTORY";
    public static final String WS_TICKET_CHAT_TRANSFORM = "CHAT_TRANSFORM";
    public static final String WS_TICKET_CHAT_RESPONSE = "CHAT_RESPONSE";
    public static final String WS_TICKET_CHAT_TRANSFORM_ACTIVE = "CHAT_TRANSFORM_ACTIVE";

    public static final String CHAT_MESSAGE_TYPE_ROBOT = "0";
    public static final String CHAT_MESSAGE_TYPE_CLIENT_TO_SERVER = "1";
    public static final String CHAT_MESSAGE_TYPE_SERVER_TO_CLIENT = "2";

    public static final String EMPTY_MESSAGE = "";

    private static SimpleWebSocket INSTANCE;

    private static final String INSTANT_MESSAGING_QUEUE = "INSTANT_MESSAGING_QUEUE";

    private static final Long MAX_SOCKET_TIME_OUT = 3 * 60 * 1000L;
}


