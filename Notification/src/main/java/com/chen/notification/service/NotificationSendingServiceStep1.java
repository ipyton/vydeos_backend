package com.chen.notification.service;

import com.alibaba.fastjson.JSON;
import com.chen.notification.entities.SingleMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.springframework.http.ResponseEntity;



public class NotificationSendingServiceStep1 {

    KafkaProducer<String, String> producer;


    public ResponseEntity<?> sendMessageHandler(SingleMessage message) {

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
