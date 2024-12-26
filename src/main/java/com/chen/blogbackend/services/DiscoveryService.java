package com.chen.blogbackend.services;

import com.alibaba.fastjson2.JSON;
import com.chen.blogbackend.entities.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class DiscoveryService {

    @Autowired
    Jedis jedisClient;

    public static final String CONSUMER_KEY = "NOTIFICATION_CONSUMER";

    public static final String PRODUCER_KEY = "NOTIFICATION_PRODUCER";

    public String queryNotificationProducerService(long userid) {
        List<String> result = jedisClient.zrangeByScore(PRODUCER_KEY,  (userid>>(16)), Double.MAX_VALUE, 0, 1);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        else return null;
    }

    public String queryNotificationConsumerService(long userid) {
        List<String> result = jedisClient.zrangeByScore(CONSUMER_KEY,  (userid>>(16)), Double.MAX_VALUE, 0, 1);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        else return null;
    }



    public Endpoint registerNotificationProducerService(Endpoint endpoint) {
        List<String> result = jedisClient.zrevrange(PRODUCER_KEY, 0, 0);
        return registerEndpoint(endpoint, result, PRODUCER_KEY);
    }

    public Endpoint registerNotificationConsumerService(Endpoint endpoint) {
        List<String> result = jedisClient.zrangeByScore(CONSUMER_KEY, 0, 0);
        return registerEndpoint(endpoint, result, CONSUMER_KEY);

    }

    private Endpoint registerEndpoint(Endpoint endpoint, List<String> result, String consumerKey) {
        if (!result.isEmpty()) {
            String maxMember = result.iterator().next();
            Double maxScore = jedisClient.zscore(consumerKey, maxMember);
            System.out.println("Member with max score: " + maxMember + ", Score: " + maxScore);
            endpoint.setStart(maxScore.intValue());
            endpoint.setEnd(maxScore.intValue() + 1);
            jedisClient.zadd(consumerKey, maxScore.intValue() + 1, JSON.toJSONString(endpoint));
        }
        else {
            endpoint.setStart(0);
            endpoint.setEnd(1);
            jedisClient.zadd(consumerKey, 0, JSON.toJSONString(endpoint));

        }
        return endpoint;
    }


}
