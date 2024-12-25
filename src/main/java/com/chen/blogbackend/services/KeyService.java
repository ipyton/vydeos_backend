package com.chen.blogbackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class KeyService {

    @Autowired
    JedisPool jedisPool;

    public int getIntKey(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 递增键的值
            int incrementedValue = (int)jedis.incr(key);

            // 打印递增后的值
            System.out.println("Key: " + key + ", Incremented Value: " + incrementedValue);
            return incrementedValue;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接池（如果不再需要使用）
            jedisPool.close();
        }
        return -1;

    }


}
