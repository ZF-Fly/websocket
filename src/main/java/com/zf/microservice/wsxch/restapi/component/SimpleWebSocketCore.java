package com.zf.microservice.wsxch.restapi.component;

import com.zf.microservice.wsxch.restapi.util.UuidUtil;
import org.springframework.context.annotation.Bean;

public final class SimpleWebSocketCore {
    private static final String FANOUT = "WSXCH_FOUNT-";
    private static final String DIRECT = "WSXCH_DIRECT-";
    private static String DIRECT_QUEUE_NAME;
    private static String FANOUT_QUEUE_NAME;
    public static final String DIRECT_EXCHANGE = "WSXCH-RABBIT-MQ-DIRECT-EXCHANGE";
    public static final String FANOUT_EXCHANGE = "WSXCH-RABBIT-MQ-FANOUT-EXCHANGE";

    static {
        DIRECT_QUEUE_NAME = DIRECT + UuidUtil.getUuid();
        FANOUT_QUEUE_NAME = FANOUT + UuidUtil.getUuid();
    }

    @Bean
    public static String getDirectQueueName() {
        return DIRECT_QUEUE_NAME;
    }

    @Bean
    public static String getFanoutQueueName() {
        return FANOUT_QUEUE_NAME;
    }
}
