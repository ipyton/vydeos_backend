package com.chen.notification.configs;


import com.datastax.oss.driver.api.core.CqlSession;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.util.Properties;

@Configuration
@Profile("endpoint")
public class EndpointConfiguration {

    @Bean
    public static Jedis configRedis() {
        Jedis jedis = new Jedis("192.168.31.75",6379);
        System.out.println("redis启动成功" + jedis.ping());
        return jedis;
    }

    @Bean
    public static KafkaConsumer<String, String> configKafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.31.75:9092");
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("group.id", "endpoint");

        return new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());
    }




}
