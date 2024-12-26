package com.chen.notification;

import com.alibaba.fastjson.JSON;
import com.chen.notification.entities.NotificationMessage;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
@Profile("dispatcher")
public class DispatcherAutoRunner {

    @Autowired
    KafkaConsumer<String, String> consumer;

    @PostConstruct
    public void run() throws Exception {
        System.out.println("This is a dispatcher service");
        consumer.subscribe(List.of("dispatch"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration. ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String topic = record.topic();
                String key = record.key();
                String value = record.value();

                NotificationMessage notificationMessage = JSON.parseObject(value, NotificationMessage.class);
                if (notificationMessage.getType().equals("single")) {
                    System.out.println("single message");
                    System.out.println(notificationMessage);
                }
                else if (notificationMessage.getType().equals("group")) {
                    System.out.println("group message");
                    System.out.println(notificationMessage);
                }
            }




//
//            for (Map.Entry<String, Map<Long, List<String>>> entry : topicKeyMap.entrySet()) {
//                String key = entry.getKey();
//                Map<Long, List<String>> keyMap = entry.getValue();
//                if (key.equals(single_topic)) {
//
//                    for (Long key1 : keyMap.keySet()) {
//                        List<SingleMessage> list = new ArrayList<>();
//                        for (String s: keyMap.get(key1)) {
//                            list.add(JSON.parseObject(s, SingleMessage.class));
//                        }
//                        service.sendMessages(list, key1);
//
//                    }
//                } else if (key.equals(group_topic)) {
//                    for (Long key1 : keyMap.keySet()) {
//                        List<GroupMessage> list = new ArrayList<>();
//                        for (String s: keyMap.get(key1)) {
//                            list.add(JSON.parseObject(s, GroupMessage.class));
//                        }
//                        service.sendMessages(list, key1);
//                    }
//                }
//            }
            consumer.commitSync();
        }

    }

}
