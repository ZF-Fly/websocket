package com.zf.microservice.wsxch.restapi.consumer;

import com.alibaba.fastjson.JSON;
import com.zf.microservice.wsxch.restapi.object.entity.WsxchRabbitMessage;
import com.zf.microservice.wsxch.restapi.component.SimpleWebSocket;
import com.zf.microservice.wsxch.restapi.component.SimpleWebSocketCore;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
public class RabbitmqConsumer {
    @Bean
    public String queueName() {
        return SimpleWebSocketCore.getDirectQueueName();
    }

    @Bean
    public String fanoutQueueName() {
        return SimpleWebSocketCore.getFanoutQueueName();
    }

    @RabbitListener(queues = "#{queueName}")
    @RabbitHandler
    public void receiveMessage(String rabbit) {
        rabbitmqMessage(rabbit);
    }

    @RabbitListener(queues = "#{fanoutQueueName}")
    @RabbitHandler
    public void receiveFanoutMessage(String rabbit) {
        rabbitmqMessage(rabbit);
    }

    private void rabbitmqMessage(String rabbit) {
        WsxchRabbitMessage rabbitMessage = JSON.parseObject(rabbit, WsxchRabbitMessage.class);
        if (!Objects.isNull(rabbitMessage)) {
            String destination = rabbitMessage.getDestination();
            String message = rabbitMessage.getMessage();
            SimpleWebSocket.send(destination, message);
        }
    }
}
