package com.zf.microservice.wsxch.restapi.component;


import com.zf.microservice.wsxch.restapi.procedure.SimpleWebSocketManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class ScheduleComponent {
    private static final Long EFFECTIVE_TIME = 3 * 60 * 1000L;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void automatic() {
        Map<String, SimpleWebSocket> instanceQueue = SimpleWebSocketManager.getInstanceQueue();
        Long now = System.currentTimeMillis();
        Set<String> keySet = new HashSet<>();
        keySet.addAll(instanceQueue.keySet());
        for (String key : keySet) {
            SimpleWebSocket simpleWebSocket = instanceQueue.get(key);
            Long activeTime = simpleWebSocket.getActiveTime();
            if (now - activeTime > EFFECTIVE_TIME) {
                simpleWebSocket.onClose();
            }
        }
    }
}
