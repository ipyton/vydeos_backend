package com.chen.notification;

import com.alibaba.fastjson.JSON;
import com.chen.notification.endpoints.NotificationServerEndpoint;
import jakarta.annotation.PostConstruct;

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

    @Value("single_topic")
    String single_topic;

    @Value("group_topic")
    String group_topic;

    private ExecutorService executorService;


    @PostConstruct
    public void startListening() throws InterruptedException, IOException {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::consumeMessages);  // 提交消费消息的任务
    }

    private void consumeMessages() {
        Properties props = new Properties();
        props.setProperty("bootstrap. servers", "localhost:9092");
        props.setProperty("group.id", "test");
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(single_topic, group_topic));
        while (true) {
            ConsumerRecords<Long, String> records = consumer.poll(Duration. ofMillis(100));
            Map<String, Map<Long, List<String>>> topicKeyMap = new HashMap<>();
            for (ConsumerRecord<Long, String> record : records) {
                String topic = record.topic();
                Long key = record.key();
                String value = record.value();

                // 获取当前 topic 对应的 key -> list map，如果不存在则创建
                topicKeyMap.putIfAbsent(topic, new HashMap<>());
                Map<Long, List<String>> keyMap = topicKeyMap.get(topic);

                // 获取当前 key 对应的 list，如果不存在则创建
                keyMap.putIfAbsent(key, new ArrayList<>());
                List<String> valueList = keyMap.get(key);

                // 将消息的 value 添加到 list 中
                valueList.add(value);
            }
        }
    }

}
