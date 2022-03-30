package com.zf.microservice.wsxch.restapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.zf.microservice")
@EnableScheduling
public class WsxchApplication {

    public static void main(String[] args) {
        SpringApplication.run(WsxchApplication.class, args);
    }

}
