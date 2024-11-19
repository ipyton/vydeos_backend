package com.chen.notification;

import com.alibaba.fastjson.JSON;
import com.chen.notification.endpoints.NotificationServerEndpoint;
import com.chen.notification.entities.GroupMessage;
import com.chen.notification.entities.SingleMessage;
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
import java.time.Duration;
import java.util.*;

/*
this class is used for changing the

 */
@Component
public class AutoRunner {

    private static final Logger logger = LoggerFactory.getLogger(AutoRunner.class);

    @Autowired
    NotificationServerEndpoint service;

    @Value("single_topic")
    String single_topic;

    @Value("group_topic")
    String group_topic;

    @PostConstruct
    public void startListening() throws InterruptedException, IOException {
        Properties props = new Properties();
        props.setProperty("bootstrap. servers", "localhost:9092");
        props.setProperty("group.id", "test");
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(single_topic, group_topic));
        final int minBatchSize = 200;
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

            for (Map.Entry<String, Map<Long, List<String>>> entry : topicKeyMap.entrySet()) {
                String key = entry.getKey();
                Map<Long, List<String>> keyMap = entry.getValue();
                if (key.equals(single_topic)) {

                    for (Long key1 : keyMap.keySet()) {
                        List<SingleMessage> list = new ArrayList<>();
                        for (String s: keyMap.get(key1)) {
                            list.add(JSON.parseObject(s, SingleMessage.class));
                        }
                        service.sendMessages(list, key1);

                    }
                } else if (key.equals(group_topic)) {
                    for (Long key1 : keyMap.keySet()) {
                        List<GroupMessage> list = new ArrayList<>();
                        for (String s: keyMap.get(key1)) {
                            list.add(JSON.parseObject(s, GroupMessage.class));
                        }
                        service.sendMessages(list, key1);
                    }
                }
            }
            consumer.commitSync();
        }

    }

}
