package com.chen.frontendserviceregistrationcenter.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.clients.jedis.Jedis;

@Configuration
@Profile("dev")
public class DevConfig {
    @Bean
    public Jedis redisClient() {
        Jedis jedis = new Jedis("192.168.2.236", 6379);
        // 如果设置 Redis 服务的密码，需要进行验证，若没有则可以省去
//        jedis.auth("123456");
        System.out.println("链接成功！");
        //查看服务是否运行
        System.out.println("服务正在运行！"+jedis.ping());
        return jedis;

    }
}
