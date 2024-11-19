package com.chen.notification.service;

import com.alibaba.fastjson.JSON;
import com.chen.notification.entities.Endpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class EndpointQueryService {

    @Autowired
    Jedis redisClient;

    public Endpoints queryEndpoint(long userId) {

        String s = redisClient.get(String.valueOf(userId / 2 << 16));

        Endpoints result = JSON.parseObject(s, Endpoints.class);
        return result;

    }
}
