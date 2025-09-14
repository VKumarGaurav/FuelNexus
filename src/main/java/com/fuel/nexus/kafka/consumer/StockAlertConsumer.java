package com.fuel.nexus.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class StockAlertConsumer {

    @KafkaListener(topics = "fuel-stock-topic", groupId = "fuel-agency-group")
    public void listen(String message) {
        System.out.println("📢 Low Stock Alert received: " + message);
    }
}


