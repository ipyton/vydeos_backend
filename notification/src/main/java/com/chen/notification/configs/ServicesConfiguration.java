package com.chen.notification.configs;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.Property;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;


@Configuration
public class ServicesConfiguration {

    @Bean
    public static Jedis configRedis() {
        Jedis jedis = new Jedis("redis.host",6379);
        System.out.println("redis启动成功" + jedis.ping());
        return jedis;
    }


}
