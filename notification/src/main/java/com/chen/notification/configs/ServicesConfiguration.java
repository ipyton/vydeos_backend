package com.chen.notification.configs;

import com.datastax.oss.driver.api.core.CqlSession;
import org.apache.kafka.clients.KafkaClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.Property;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.util.Properties;


@Configuration
public class ServicesConfiguration {

    @Bean
    public static Jedis configRedis() {
        Jedis jedis = new Jedis("redis.host",6379);
        System.out.println("redis启动成功" + jedis.ping());
        return jedis;
    }

    @Bean
    public static KafkaProducer<String, String> configKafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("transactional.id", "my-transactional-id");
        return new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
    }

    @Bean
    public static KafkaConsumer<String, String> configKafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "my-group");
        props.put("enable.auto.commit", "true");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());
        return consumer;
    }

    @Bean
    public static CqlSession setScyllaSession(){

        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress("192.168.2.75",9042))
                .withAuthCredentials("cassandra", "cassandra")
                .withLocalDatacenter("datacenter1").build();

    }
}
