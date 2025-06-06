package com.chen.notification.service;

import com.alibaba.fastjson.JSON;
import com.chen.notification.entities.ActiveStatus;
import com.chen.notification.entities.GroupMessage;
import com.chen.notification.entities.SingleMessage;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import io.lettuce.core.RedisClient;
import io.netty.util.concurrent.DefaultThreadFactory;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.*;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;


public class NotificationSendingServiceStep2 {
    private static final String KAFKA_BROKER = "localhost:9092"; // Update with your Kafka broker
    private static final String TOPIC_ALL_MESSAGES = "all_messages";
    private static final String TOPIC_MESSAGE_SENDING_ERRORS = "message_sending_errors";
    private static final String TOPIC_SINGLE_MESSAGE = "singleMessage";
    private static final String GROUP_ID = "my-consumer-group";

    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;
    private KafkaProducer<String, String> singleMessageProducer;

    Jedis jedis;

    CqlSession session;

    PreparedStatement prepare;



    public static class NamedThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private int threadId = 0;

        public NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + "-Thread-" + threadId++);
            System.out.println("Created: " + thread.getName());
            return thread;
        }

    }


    private ActiveStatus getActiveStatus(String userId) {
        // Replace with actual Redis or database logic to get the active status
        // Simulating a missing status in this example
        String activeStatus = jedis.hget("activeStatus", userId);
        return JSON.parseObject(activeStatus, ActiveStatus.class); // Simulated response
    }

    private Properties getConsumerConfig() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return props;
    }

    private Properties getProducerConfig(String topic) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, topic);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }

    @PostConstruct
    public void init() {
        consumer = new KafkaConsumer<>(getConsumerConfig());
        producer = new KafkaProducer<>(getProducerConfig(TOPIC_MESSAGE_SENDING_ERRORS));
        singleMessageProducer = new KafkaProducer<>(getProducerConfig(TOPIC_SINGLE_MESSAGE));
        prepare = session.prepare("select * from chat_group_by_id where group_id = ?");
    }

    private void handleGroupMessage(GroupMessage message, org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        String groupId = message.getReceiverId();
        // Simulate retrieving user ids from database
        // Replace this with your actual database query logic

        ResultSet execute = session.execute(prepare.bind(groupId));
        execute.forEach((res) -> {
            String userId = res.get(0, String.class);
                // Send the message to single message producer
            SingleMessage singleMessage = new SingleMessage();
            handleSingleMessage(userId, singleMessage, record);
        });


    }

    private void sendErrorMessage(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        // Send error to the error topic
        ProducerRecord<String, String> errorRecord = new ProducerRecord<>(TOPIC_MESSAGE_SENDING_ERRORS, record.key(), record.value());
        producer.send(errorRecord);
    }

//    public void start() {
//        // Subscribe to the topic
//        consumer.subscribe(Collections.singletonList(TOPIC_ALL_MESSAGES));
//        int corePoolSize = 4;    // 核心线程数
//        int maximumPoolSize = 8; // 最大线程数
//        long keepAliveTime = 60; // 空闲线程存活时间
//        TimeUnit timeUnit = TimeUnit.SECONDS; // 时间单位
//        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(100); // 任务队列
//        ThreadFactory threadFactory =  new NamedThreadFactory("Step2");
//        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
//
//            @Override
//            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
//                if (!executor.isShutdown()) {
//                    r.run();
//                }
//                RestTemplate restTemplate = new RestTemplate();
//                restTemplate.postForEntity("localhost:9092", r, String.class);
//
//            }
//        };
//
//        // 创建线程池
//        ThreadPoolExecutor executor = new ThreadPoolExecutor(
//                corePoolSize,
//                maximumPoolSize,
//                keepAliveTime,
//                timeUnit,
//                workQueue,
//                threadFactory,
//                handler
//        );
//
//
//
//
//        while (true) {
//            // Poll for messages
//            var records = consumer.poll(1000);
//            records.forEach(record -> {
//                try {
//                    // Deserialize the message
//                    NotificationMessage message = JSON.parseObject(record.value(), NotificationMessage.class);
//                    executor.execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            if ("group".equals(message.getType())) {
//                                handleGroupMessage(message, record);
//                            } else {
//                                // Handle single message type here
//                                handleSingleMessage(message.getReceiverId(), message,record);
//                            }
//                            consumer.commitAsync();
//                        }
//                    });
//
//                } catch (Exception e) {
//                    sendErrorMessage(record);
//                    e.printStackTrace();
//                }
//            });
//        }
//    }

    public void handleSingleMessage(String userId, SingleMessage message, org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        ActiveStatus status = getActiveStatus(userId);  // Simulate retrieving user status
        if (status!=null) {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_SINGLE_MESSAGE, userId, JSON.toJSONString(message));
            singleMessageProducer.send(producerRecord);
            System.out.println("User ID: " + userId);
        }
        else {
            sendErrorMessage(record);
        }
    }


}
