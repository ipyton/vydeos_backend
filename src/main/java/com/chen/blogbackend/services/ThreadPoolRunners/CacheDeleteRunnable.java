package com.chen.blogbackend.services.ThreadPoolRunners;

import com.chen.blogbackend.components.ConcurrentLatencyEstimator;
import com.datastax.oss.driver.api.core.CqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheDeleteRunnable implements Runnable {
    String key;
    String cqlStatement;

    ConcurrentLatencyEstimator estimator;
    ScheduledExecutorService scheduler;
    Jedis jedis;
    CqlSession cqlSession;

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        jedis.del(key);

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                jedis.del(key);
            }
        }, estimator.getLatency(), TimeUnit.MILLISECONDS);
        cqlSession.execute(cqlStatement);



    }

    public CacheDeleteRunnable(String cqlStatement, String key, ScheduledExecutorService service, ConcurrentLatencyEstimator estimator, Jedis jedis, CqlSession session) {
        this.key = key;
        this.scheduler = service;
        this.estimator = estimator;
        this.jedis = jedis;
        this.cqlSession = session;

    }
}
