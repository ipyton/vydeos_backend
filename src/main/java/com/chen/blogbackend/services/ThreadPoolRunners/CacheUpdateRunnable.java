package com.chen.blogbackend.services.ThreadPoolRunners;

import com.chen.blogbackend.util.RandomUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheUpdateRunnable implements Runnable {
    String type = "";
    String key;
    String value;
    AtomicInteger ;
    AtomicInteger count;
    ScheduledExecutorService scheduler;


    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();


        RandomUtil.
        Thread.sleep();



    }

    public CacheUpdateRunnable(String type, String key, String value, AtomicInteger upperBound, ScheduledExecutorService service) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.upperBound = upperBound;
        this.scheduler = service;
    }
}
