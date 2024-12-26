package com.chen.notification.controller;

import com.chen.notification.entities.NotificationMessage;
import com.chen.notification.service.NotificationSendingServiceStep1;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Properties;

@RestController

public class MessageController {

    @Autowired
    NotificationSendingServiceStep1 sendingServiceStep1;

    @PostMapping("/send-message")
    public ResponseEntity<?> sendMessage(@RequestBody NotificationMessage message) {
        // Step 1: Initialize Kafka producer
        Properties properties = new Properties();
        return sendingServiceStep1.sendMessageHandler(message);

    }
}
