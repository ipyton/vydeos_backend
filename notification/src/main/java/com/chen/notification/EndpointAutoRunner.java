package com.chen.notification;

import com.alibaba.fastjson.JSON;
import com.chen.notification.endpoints.NotificationServerEndpoint;
import com.chen.notification.entities.NotificationMessage;
import jakarta.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
this class is used for changing the

 */
@Component
@Profile("endpoint")
public class EndpointAutoRunner {


    @Autowired
    NotificationServerEndpoint service;

    @Autowired
    KafkaConsumer<String, String> consumer;

    @Value("single_topic")
    String single_topic;

    @Value("group_topic")
    String group_topic;

    private ExecutorService executorService;



    @PostConstruct
    private void consumeMessages() {

        consumer.subscribe(Arrays.asList("single"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration. ofMillis(100));
            Map<String, List<NotificationMessage>> topicKeyMap = new HashMap<>();
            for (ConsumerRecord<String, String> record : records) {
                String topic = record.topic();
                String key = record.key();
                String value = record.value();
                System.out.println(value);
                topicKeyMap.putIfAbsent(key, new ArrayList<>());
                topicKeyMap.get(key).add(JSON.parseObject(value, NotificationMessage.class));


            }

            consumer.commitSync();
        }
    }

}
