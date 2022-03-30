package com.zf.microservice.wsxch.restapi.config;

import com.zf.microservice.wsxch.restapi.component.SimpleWebSocketCore;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
    @Bean
    public Queue getDirectQueue() {
        return new Queue(SimpleWebSocketCore.getDirectQueueName(), true, false, true);
    }
    @Bean
    public Queue getFanoutQueue() {
        return new Queue(SimpleWebSocketCore.getFanoutQueueName(), true, false, true);
    }
    @Bean
    DirectExchange getDirectExchange() {
        return new DirectExchange(SimpleWebSocketCore.DIRECT_EXCHANGE);
    }
    @Bean
    FanoutExchange getFanoutExchange() {
        return new FanoutExchange(SimpleWebSocketCore.FANOUT_EXCHANGE);
    }

    @Bean
    Binding bindingDirect() {
        return BindingBuilder.bind(getDirectQueue()).to(getDirectExchange()).with(SimpleWebSocketCore.getDirectQueueName());
    }
    @Bean
    Binding bindingFanout() {
        return BindingBuilder.bind(getFanoutQueue()).to(getFanoutExchange());
    }
}
