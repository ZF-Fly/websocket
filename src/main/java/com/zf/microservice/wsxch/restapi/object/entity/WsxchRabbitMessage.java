package com.zf.microservice.wsxch.restapi.object.entity;

import java.io.Serializable;

public class WsxchRabbitMessage implements Serializable {

    public WsxchRabbitMessage (String destination, String message) {
        this.message = message;
        this.destination = destination;
    }

    private String message;
    private String destination;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
}