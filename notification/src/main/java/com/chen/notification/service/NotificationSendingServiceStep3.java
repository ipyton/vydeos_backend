package com.chen.notification.service;

import com.alibaba.fastjson.JSON;
import com.chen.notification.entities.NotificationMessage;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class NotificationSendingServiceStep3 {
    private static final String TOPIC_SINGLE_MESSAGE = "singleMessage";
    private KafkaConsumer<String, String> consumer;
    private static final String KAFKA_BROKER = "localhost:9092"; // Update with your Kafka broker
    private static final String GROUP_ID = "my-consumer-group";
    private static final String TOPIC_MESSAGE_SENDING_ERRORS = "message_sending_errors";
    private KafkaProducer<String, String> errorProducer;


    private Properties getProducerConfig(String topic) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, topic);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }

    @PostConstruct
    public void Init() {
        consumer = new KafkaConsumer<>(getConsumerConfig());
        errorProducer = new KafkaProducer<>(getProducerConfig(TOPIC_MESSAGE_SENDING_ERRORS));

    }

    private void sendErrorMessage(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        // Send error to the error topic
        ProducerRecord<String, String> errorRecord = new ProducerRecord<>(TOPIC_MESSAGE_SENDING_ERRORS, record.key(), record.value());
        errorProducer.send(errorRecord);
    }

    public void start() {
        while (true) {
            // Poll for messages
            var records = consumer.poll(1000);
            records.forEach(record -> {
                try {
                    // Deserialize the message
                    NotificationMessage message = JSON.parseObject(record.value(), NotificationMessage.class);
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

    private Properties getConsumerConfig() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return props;
    }
}
