package com.chen.blogbackend.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

@Service
public class ActiveStatusService {

    @Autowired
    JedisPool pool;
    public void registration() {
        pool.getResource().get("");
    }


}
