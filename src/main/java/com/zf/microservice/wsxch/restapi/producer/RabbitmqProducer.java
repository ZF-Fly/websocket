package com.zf.microservice.wsxch.restapi.producer;

import com.alibaba.fastjson.JSON;
import com.zf.microservice.wsxch.restapi.object.entity.WsxchRabbitMessage;
import com.zf.microservice.wsxch.restapi.component.SimpleWebSocketCore;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class RabbitmqProducer {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendWithQueueName(WsxchRabbitMessage rabbitMessage, String queueName) {
        amqpTemplate.convertAndSend(
                SimpleWebSocketCore.DIRECT_EXCHANGE,
                queueName,
                JSON.toJSONString(rabbitMessage));
    }

    public void sendWithQueueName(String rabbitMessage, String queueName) {
        amqpTemplate.convertAndSend(
                queueName,
                rabbitMessage);
    }
    public void sendFanoutMessage(WsxchRabbitMessage rabbitMessage) {
        amqpTemplate.convertAndSend(
                SimpleWebSocketCore.FANOUT_EXCHANGE,
                "",
                JSON.toJSONString(rabbitMessage));
    }
}
