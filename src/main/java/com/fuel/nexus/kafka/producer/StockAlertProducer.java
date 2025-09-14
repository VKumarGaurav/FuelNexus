package com.fuel.nexus.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockAlertProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public StockAlertProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendLowStockAlert(String message) {
        kafkaTemplate.send("fuel-stock-topic", message);
    }
}

