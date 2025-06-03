package com.chen.notification.service;

import com.chen.notification.entities.RegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;

public class RegistrationService {

    Jedis redisClient;
    enum CapacityStatus {
        AVAILABLE, NEEDED, EMERGENCY
    }
    private CapacityStatus checkCapacity(String candidate, double score) {
        return CapacityStatus.AVAILABLE;
    }

    public RegistrationResponse register(String userId) {
        RegistrationResponse response = new RegistrationResponse();
        String key = "endpoints_weights";
        RestTemplate restTemplate = new RestTemplate();
        // endpoints 多线程访问
        while (true) {
            String candidate = redisClient.zrangeByScore(key, 0, 0).iterator().next();
            Double minScore = redisClient.zscore(key, candidate);
            String endpoint_meta = redisClient.hget("endpoints", candidate);
            if (endpoint_meta == null) {
                redisClient.zrem(key, candidate);
                continue;
            }
            if (checkCapacity(candidate, minScore).equals(CapacityStatus.NEEDED)) {
                //change to micro service in the future
                restTemplate.postForEntity("http://localhost:8080/scaleUp",null,null);
            } else if (checkCapacity(candidate, minScore).equals(CapacityStatus.EMERGENCY)) {
                response.code = -1;
                response.status = "No capacity available";
                restTemplate.postForEntity("http://localhost:8080/scaleUp",null,null);
                return response;
            }
            response.status = "success";
            response.code = 0;
            response.endpointAddress = candidate.split("_")[1];
            redisClient.zincrby(key, 1, candidate);
            redisClient.hset("active_status", userId, endpoint_meta);
            return response;
        }

    }
}
