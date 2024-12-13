package com.chen.notification.service;

import com.alibaba.fastjson.JSON;
import com.chen.notification.entities.ActiveStatus;
import com.chen.notification.entities.NotificationMessage;
import io.lettuce.core.RedisClient;
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
import org.apache.kafka.clients.consumer.ConsumerConfig;
import redis.clients.jedis.Jedis;


@Service
public class NotificationSendingServiceStep2 {
    private static final String KAFKA_BROKER = "localhost:9092"; // Update with your Kafka broker
    private static final String TOPIC_ALL_MESSAGES = "all_messages";
    private static final String TOPIC_MESSAGE_SENDING_ERRORS = "message_sending_errors";
    private static final String TOPIC_SINGLE_MESSAGE = "singleMessage";
    private static final String GROUP_ID = "my-consumer-group";

    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;
    private KafkaProducer<String, String> singleMessageProducer;

    @Autowired
    Jedis jedis;

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
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }
    @PostConstruct
    public void init() {
        consumer = new KafkaConsumer<>(getConsumerConfig());
        producer = new KafkaProducer<>(getProducerConfig(TOPIC_MESSAGE_SENDING_ERRORS));
        singleMessageProducer = new KafkaProducer<>(getProducerConfig(TOPIC_SINGLE_MESSAGE));
    }
    private void handleGroupMessage(NotificationMessage message, org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        String groupId = message.getReceiverId();
        // Simulate retrieving user ids from database
        // Replace this with your actual database query logic
        String userId = "someUserId";  // Get user ID from DB based on groupId

        ActiveStatus status = getActiveStatus(userId);  // Simulate retrieving user status

        if (status != null) {
            // Send the message to single message producer
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_SINGLE_MESSAGE, userId, new Gson().toJson(message));
            singleMessageProducer.send(producerRecord);
            System.out.println("User ID: " + userId);
        } else {
            // If status is null, send error message
            sendErrorMessage(record);
        }
    }

    private void sendErrorMessage(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        // Send error to the error topic
        ProducerRecord<String, String> errorRecord = new ProducerRecord<>(TOPIC_MESSAGE_SENDING_ERRORS, record.key(), record.value());
        producer.send(errorRecord);
    }

    public void start() {
        // Subscribe to the topic
        consumer.subscribe(Collections.singletonList(TOPIC_ALL_MESSAGES));

        while (true) {
            // Poll for messages
            var records = consumer.poll(1000);
            records.forEach(record -> {
                try {
                    // Deserialize the message
                    NotificationMessage message = JSON.parseObject(record.value(), NotificationMessage.class);

                    if ("group".equals(message.getType())) {
                        handleGroupMessage(message, record);
                    } else {
                        // Handle single message type here
                        handleSingleMessage();
                    }

                    // Acknowledge message by committing the offset
                    consumer.commitAsync();
                } catch (Exception e) {
                    // Handle error and send to error topic
                    sendErrorMessage(record);
                    e.printStackTrace();
                }
            });
        }
    }
    public void handleSingleMessage() {

    }

    public void sendMessageHandler(NotificationMessage message) {

    }
}
