package com.zf.microservice.wsxch.restapi.config;

/**
 * @author Fly_Roushan
 * @date 2021/8/18
 */
public class RedisListenerProperty {

    private String listenerName;
    private String listenerKey;

    public String getListenerName() {
        return listenerName;
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }

    public String getListenerKey() {
        return listenerKey;
    }

    public void setListenerKey(String listenerKey) {
        this.listenerKey = listenerKey;
    }
}
