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
        try  {
            // 递增键的值
            Jedis jedis = jedisPool.getResource();
            int incrementedValue = (int)jedis.incr(key);

            // 打印递增后的值
            System.out.println("Key: " + key + ", Incremented Value: " + incrementedValue);
            jedis.close();
            return incrementedValue;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return -1;

    }

    public long getLongKey(String key) {
        try {
            // 递增键的值
            Jedis jedis = jedisPool.getResource();
            long incrementedValue = jedis.incr(key);

            // 打印递增后的值
            System.out.println("Key: " + key + ", Incremented Value: " + incrementedValue);
            jedis.close();
            return incrementedValue;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return -1L;
    }


}
