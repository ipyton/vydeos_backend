package com.chen.blogbackend.services;

import com.alibaba.fastjson2.JSON;
import com.chen.blogbackend.entities.SingleMessage;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationProducer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationProducer.class);

    @Autowired
    Producer<String, String> producer;

    ThreadPoolExecutor executorService;

    @PostConstruct
    public void init() {
        executorService = new ThreadPoolExecutor(2,4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void sendNotification(SingleMessage message ) {
//        int partitions = 0;
//        MessageBuilder builder = new MessageBuilderImpl();
//        int i = message.getUserId().hashCode();
//        Message messageToSend = builder.setTopic("notificationTopic").setKeys().setTag("1")
//                .setBody(JSON.toJSONBytes(message)).build();
//
//        SendReceipt send = producer.send(messageToSend);
//        MessageId messageId = send.getMessageId();
//        System.out.println("message Id" + messageId);
        System.out.println("dispatch");
    producer.send(new ProducerRecord<>("dispatch", message.getUserId2() ,JSON.toJSONString(message)));
    }






}
