package com.chen.notification.service;

import com.alibaba.fastjson.JSON;
import com.chen.notification.entities.NotificationMessage;
import org.springframework.stereotype.Service;
import com.chen.notification.service.NotificationSendingServiceStep1;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Properties;

@Service
public class NotificationSendingServiceStep1 {

    @Autowired
    KafkaProducer<String, String> producer;


    public ResponseEntity<?> sendMessageHandler(NotificationMessage message) {

        String jsonString = JSON.toJSONString(message);
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>("messages", null, jsonString);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    // If there's an error while sending
                    exception.printStackTrace();
                } else {
                    // Successfully sent the message
                    System.out.println("Message sent to topic: messages"   + " with offset: " + metadata.offset());
                }
            });
        } catch (Exception e) {
            producer.close();
            return ResponseEntity.status(500).body("Failed to send message to Kafka: " + e.getMessage());
        }

        // Close the producer
        producer.close();

        // Step 4: Respond with success
        return ResponseEntity.ok("Message sent successfully");
    }
}
