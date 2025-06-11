package com.chen.blogbackend.services;

import com.alibaba.fastjson2.JSON;
import com.chen.blogbackend.entities.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class DiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(DiscoveryService.class);

    @Autowired
    Jedis jedisClient;

    public static final String CONSUMER_KEY = "NOTIFICATION_CONSUMER";
    public static final String PRODUCER_KEY = "NOTIFICATION_PRODUCER";

    public String queryNotificationProducerService(long userid) {
        logger.debug("Querying notification producer service for userId: {}", userid);

        try {
            long shardId = userid >> 16;
            logger.debug("Calculated shardId: {} for userId: {}", shardId, userid);

            List<String> result = jedisClient.zrangeByScore(PRODUCER_KEY, shardId, Double.MAX_VALUE, 0, 1);

            if (!result.isEmpty()) {
                String producer = result.get(0);
                logger.info("Found notification producer service for userId: {} - {}", userid, producer);
                return producer;
            } else {
                logger.warn("No notification producer service found for userId: {}", userid);
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to query notification producer service for userId: {} - {}",
                    userid, e.getMessage(), e);
            return null;
        }
    }

    public String queryNotificationConsumerService(long userid) {
        logger.debug("Querying notification consumer service for userId: {}", userid);

        try {
            long shardId = userid >> 16;
            logger.debug("Calculated shardId: {} for userId: {}", shardId, userid);

            List<String> result = jedisClient.zrangeByScore(CONSUMER_KEY, shardId, Double.MAX_VALUE, 0, 1);

            if (!result.isEmpty()) {
                String consumer = result.get(0);
                logger.info("Found notification consumer service for userId: {} - {}", userid, consumer);
                return consumer;
            } else {
                logger.warn("No notification consumer service found for userId: {}", userid);
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to query notification consumer service for userId: {} - {}",
                    userid, e.getMessage(), e);
            return null;
        }
    }

    public Endpoint registerNotificationProducerService(Endpoint endpoint) {
        logger.info("Registering notification producer service - endpoint: {}", endpoint);

        try {
            List<String> result = jedisClient.zrevrange(PRODUCER_KEY, 0, 0);
            Endpoint registeredEndpoint = registerEndpoint(endpoint, result, PRODUCER_KEY);

            logger.info("Successfully registered notification producer service - endpoint: {}, range: {}-{}",
                    registeredEndpoint, registeredEndpoint.getStart(), registeredEndpoint.getEnd());

            return registeredEndpoint;
        } catch (Exception e) {
            logger.error("Failed to register notification producer service - endpoint: {} - {}",
                    endpoint, e.getMessage(), e);
            throw new RuntimeException("Failed to register notification producer service", e);
        }
    }

    public Endpoint registerNotificationConsumerService(Endpoint endpoint) {
        logger.info("Registering notification consumer service - endpoint: {}", endpoint);

        try {
            List<String> result = jedisClient.zrangeByScore(CONSUMER_KEY, 0, 0);
            Endpoint registeredEndpoint = registerEndpoint(endpoint, result, CONSUMER_KEY);

            logger.info("Successfully registered notification consumer service - endpoint: {}, range: {}-{}",
                    registeredEndpoint, registeredEndpoint.getStart(), registeredEndpoint.getEnd());

            return registeredEndpoint;
        } catch (Exception e) {
            logger.error("Failed to register notification consumer service - endpoint: {} - {}",
                    endpoint, e.getMessage(), e);
            throw new RuntimeException("Failed to register notification consumer service", e);
        }
    }

    private Endpoint registerEndpoint(Endpoint endpoint, List<String> result, String serviceKey) {
        logger.debug("Registering endpoint with serviceKey: {}, existing results count: {}",
                serviceKey, result.size());

        try {
            if (!result.isEmpty()) {
                String maxMember = result.iterator().next();
                Double maxScore = jedisClient.zscore(serviceKey, maxMember);

                logger.debug("Found existing member with max score - member: {}, score: {}", maxMember, maxScore);

                if (maxScore == null) {
                    logger.warn("Max score is null for member: {}, defaulting to 0", maxMember);
                    maxScore = 0.0;
                }

                int newStart = maxScore.intValue();
                int newEnd = newStart + 1;

                endpoint.setStart(newStart);
                endpoint.setEnd(newEnd);

                String endpointJson = JSON.toJSONString(endpoint);
                jedisClient.zadd(serviceKey, newEnd, endpointJson);

                logger.debug("Registered endpoint with existing members - start: {}, end: {}, json: {}",
                        newStart, newEnd, endpointJson);
            } else {
                logger.debug("No existing members found, registering first endpoint");

                endpoint.setStart(0);
                endpoint.setEnd(1);

                String endpointJson = JSON.toJSONString(endpoint);
                jedisClient.zadd(serviceKey, 0, endpointJson);

                logger.debug("Registered first endpoint - start: 0, end: 1, json: {}", endpointJson);
            }

            logger.info("Successfully registered endpoint for serviceKey: {} - range: {}-{}",
                    serviceKey, endpoint.getStart(), endpoint.getEnd());

            return endpoint;
        } catch (Exception e) {
            logger.error("Failed to register endpoint for serviceKey: {} - {}", serviceKey, e.getMessage(), e);
            throw new RuntimeException("Failed to register endpoint", e);
        }
    }
}