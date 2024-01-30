package com.chen.blogbackend.services;

import com.alibaba.fastjson2.JSON;
import com.chen.blogbackend.entities.Notification;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.message.MessageId;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.java.message.MessageBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationProducer.class);

    @Autowired
    Producer producer;

    public void sendNotification(Notification notification) throws ClientException {
        MessageBuilder builder = new MessageBuilderImpl();
        int i = notification.getName().hashCode();
        Message message = builder.setTopic("notification").setKeys().setTag("" + i)
                .setBody(JSON.toJSONBytes(notification)).build();

        SendReceipt send = producer.send(message);
        MessageId messageId = send.getMessageId();
        System.out.println("message Id" + messageId);
    }






}
