package com.chen.notification;

import com.alibaba.fastjson.JSON;
import com.chen.notification.endpoints.NotificationServerEndpoint;
import com.chen.notification.entities.NotificationMessage;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
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

//    @Autowired
//    KafkaProducer<String, String> DLQ;

    @Autowired
    private  CqlSession session;

    PreparedStatement getEndpoints;

    @Value("single_topic")
    String single_topic;

    @Value("group_topic")
    String group_topic;

    @Autowired
    PushService sender;

    private ExecutorService executorService;

    public static class WebPushMessage {

        public String title;
        public String clickTarget;
        public String message;
        public String publicKey;
        public String privateKey;
        public String endpoint;
    }
    ObjectMapper mapper = new ObjectMapper();



    @PostConstruct
    private void run(){
        executorService = Executors.newFixedThreadPool(4); // 根据需要调整线程池大小
        getEndpoints = session.prepare("select endpoint,auth,p256dh from chat.web_push_endpoints where user_id = ?");
        // 使用新线程异步启动 Kafka 消费
        new Thread(this::consumeMessages).start();
    }


    private void consumeMessages()  {
        try {
        consumer.subscribe(Arrays.asList("single"));
            RestTemplate restTemplate = new RestTemplate();

            while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration. ofMillis(100));
            Map<String, List<NotificationMessage>> topicKeyMap = new HashMap<>();
            for (ConsumerRecord<String, String> record : records) {
//                String topic = record.topic();
                String key = record.key();
                String value = record.value();
                System.out.println(value);
                topicKeyMap.putIfAbsent(key, new ArrayList<>());
                NotificationMessage notificationMessage = JSON.parseObject(value, NotificationMessage.class);
                topicKeyMap.get(key).add(notificationMessage);
                ResultSet execute = session.execute(getEndpoints.bind(notificationMessage.getReceiverId()));
                if (execute.getExecutionInfo().getErrors().isEmpty()) {
                    for (Row row : execute.all()) {
                        String auth = row.getString("auth");
                        String endpoint = row.getString("endpoint");
                        String p256dh = row.getString("p256dh");
                        if (auth == null || p256dh == null) {
                            continue;
                        }
//                        Subscription subscription = new Subscription(
//                                endpoint, // endpoint
//                                new Subscription.Keys(p256dh, auth)                                 // auth key
//                        );
                        System.out.println(endpoint);
                        System.out.println(p256dh);
                        System.out.println(auth);
                        WebPushMessage webPushMessage = new WebPushMessage();
                        webPushMessage.title = "hello";
                        webPushMessage.message = JSON.toJSONString(notificationMessage);
                        webPushMessage.clickTarget = "www.baidu.com";
                        webPushMessage.endpoint = endpoint;
                        webPushMessage.publicKey = p256dh;
                        webPushMessage.privateKey = auth;
//                        HttpResponse send = sender.send(new Notification(endpoint, p256dh, auth, mapper.writeValueAsBytes(webPushMessage)));
//                        System.out.println(send.toString());
//                        int statusCode = send.getStatusLine().getStatusCode();
                        restTemplate.postForObject("http://localhost:5000/send", webPushMessage, String.class);
                    }
                    service.sendMessages(List.of(notificationMessage));

                }
                else {
//                    DLQ.send(new ProducerRecord<>(notificationMessage.getReceiverId(), value));

                }
            }
            consumer.commitSync();
        }
        } catch (Exception e) {
            System.err.println("Error while consuming messages: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
