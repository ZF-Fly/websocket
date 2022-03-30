package com.zf.microservice.wsxch.restapi.util;


import com.alibaba.fastjson.JSONObject;
import com.zf.microservice.wsxch.restapi.object.constant.Constants;
import com.zf.microservice.wsxch.restapi.object.constant.InstantMessagingStatusConst;

/**
 *
 */
public class MessageConversionUtil {
    public static String convertInstantMessaging(JSONObject dataJson) {
        JSONObject message = new JSONObject();
        if (dataJson.containsKey(Constants.LOAN_ID)) {
            message.put(Constants.LOAN_ID, dataJson.getLong(Constants.LOAN_ID));
        } else {
            throw new NullPointerException();
        }
        if (dataJson.containsKey(Constants.TICKET)) {
            message.put(Constants.TICKET, dataJson.getString(Constants.TICKET));
        }
        if (dataJson.containsKey(Constants.AGENT_ID)) {
            message.put(Constants.AGENT_ID, dataJson.getLong(Constants.AGENT_ID));
        }
        if (dataJson.containsKey(Constants.TYPE)) {
            message.put(Constants.TYPE, dataJson.getInteger(Constants.TYPE));
        }
        if (dataJson.containsKey(Constants.MESSAGE)) {
            JSONObject messageJson = dataJson.getJSONObject(Constants.MESSAGE);
            if (messageJson.containsKey(Constants.MESSAGE)) {
                message.put(Constants.MESSAGE, messageJson.getString(Constants.MESSAGE));
            }
            if (messageJson.containsKey(Constants.SENDER)) {
                message.put(Constants.SENDER, messageJson.getString(Constants.SENDER));
            }
        }
        message.put(Constants.SENT_AT, System.currentTimeMillis());
        message.put(Constants.STATUS, InstantMessagingStatusConst.SUCCESS);
        return message.toJSONString();
    }

    public static String convertInstantChatStatistics(JSONObject dataJson) {
        JSONObject messageJson = new JSONObject();
        if (dataJson.containsKey(Constants.LOAN_ID)) {
            messageJson.put(Constants.LOAN_ID, dataJson.getLong(Constants.LOAN_ID));
        } else {
            throw new NullPointerException();
        }
        if (dataJson.containsKey(Constants.TICKET)) {
            messageJson.put(Constants.TICKET, dataJson.getString(Constants.TICKET));
        }
        if (dataJson.containsKey(Constants.AGENT_ID)) {
            messageJson.put(Constants.AGENT_ID, dataJson.getLong(Constants.AGENT_ID));
        }
        if (dataJson.containsKey(Constants.STARTED_PAGE)) {
            messageJson.put(Constants.STARTED_PAGE, dataJson.getLong(Constants.STARTED_PAGE));
        }
        messageJson.put(Constants.STARTED_AT, System.currentTimeMillis());
        return messageJson.toJSONString();
    }

}
