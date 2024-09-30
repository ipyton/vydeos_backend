package com.chen.notification;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chen.notification.endpoints.NotificationServerEndpoint;
import com.chen.notification.entities.Notification;
import com.chen.notification.entities.SingleMessage;
import com.chen.notification.service.SendNotificationService;
import com.chen.notification.utils.ConfigUtil;
import jakarta.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Component
public class AutoRunner {

    private static final Logger logger = LoggerFactory.getLogger(AutoRunner.class);

    @Autowired
    NotificationServerEndpoint service;

    @Value("notification_topic")
    String topic;

    @PostConstruct
    public void startListening() throws InterruptedException {
        Properties props = new Properties();
        props.setProperty("bootstrap. servers", "localhost:9092");
        props.setProperty("group.id", "test");
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));
        final int minBatchSize = 200;
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration. ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                buffer.add(record);
            }
            buffer.forEach(record -> {
                SingleMessage jsonObject = JSON.parseObject(record.value(),SingleMessage.class );
                try {
                    service.sendMessage(jsonObject);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            consumer.commitSync();
            buffer.clear();
        }

    }

}
