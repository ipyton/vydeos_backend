package com.chen.frontendserviceregistrationcenter.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class DiscoveryService {

    @Autowired
    Jedis jedisClient;


    public String queryNotificationSenderService(String userid) {
        return jedisClient.get( "notification_sender_"  + (Long.parseLong(userid) >> (16)));

    }

    public String queryNotificationReceiverService(String userid) {

        return jedisClient.get( "notification_receiver_"  + (Long.parseLong(userid) >> (16)));

    }
}
