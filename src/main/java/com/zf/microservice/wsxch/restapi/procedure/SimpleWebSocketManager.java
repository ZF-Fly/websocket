package com.zf.microservice.wsxch.restapi.procedure;

import com.alibaba.fastjson.JSONObject;
import com.zf.microservice.wsxch.restapi.component.SimpleWebSocket;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SimpleWebSocketManager {
    public static void addInstance(SimpleWebSocket instance) {
        String wsid = instance.getWsid();
        INSTANCE_QUEUE.put(wsid, instance);
    }


    public static SimpleWebSocket getInstance(String wsid) {
        SimpleWebSocket destinationInstance = INSTANCE_QUEUE.getOrDefault(wsid, null);
        return destinationInstance;
    }

    public static Map<String, SimpleWebSocket> getInstanceQueue() {
        return INSTANCE_QUEUE;
    }

    public static void removeInstance(String wsid) {
        SERVER_COUNTER.remove(wsid);
        INSTANCE_QUEUE.remove(wsid);
    }

    public static String getLeastConnectionServer(JSONObject serverCounter) {
        String leastConnectionServer = null;
        Integer leastCount = -1;
        for (String wsid : serverCounter.keySet()) {
            Integer count = serverCounter.getInteger(wsid);
            if(StringUtils.isEmpty(wsid)) {
                continue;
            }
            if (leastCount == -1 || count < leastCount) {
                leastConnectionServer = wsid;
                leastCount = count;
            }
        }
        serverCounter.put(leastConnectionServer, ++leastCount);
        return leastConnectionServer;
    }

    public static void serverSignin(String serverWsid) {
        if (!SERVER_COUNTER.containsKey(serverWsid)) {
            SERVER_COUNTER.put(serverWsid, 0);
        }
    }

    public static ConcurrentHashMap<String, Integer> getServerCounter() {
        return SERVER_COUNTER;
    }

    private static ConcurrentHashMap<String, Integer> SERVER_COUNTER = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, SimpleWebSocket> INSTANCE_QUEUE = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, List<SimpleWebSocket>> INSTANCE_QUEUE_LIST = new ConcurrentHashMap<>();

}
