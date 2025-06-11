package com.chen.blogbackend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class KeyService {

    private static final Logger logger = LoggerFactory.getLogger(KeyService.class);

    @Autowired
    JedisPool jedisPool;

    public int getIntKey(String key) {
        logger.debug("Getting integer key for: {}", key);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            int incrementedValue = (int) jedis.incr(key);

            logger.info("Successfully incremented key: {}, new value: {}", key, incrementedValue);
            return incrementedValue;

        } catch (Exception e) {
            logger.error("Error incrementing integer key: {}", key, e);
            return -1;
        } finally {
            if (jedis != null) {
                try {
                    jedis.close();
                    logger.debug("Jedis connection closed for key: {}", key);
                } catch (Exception e) {
                    logger.warn("Error closing Jedis connection for key: {}", key, e);
                }
            }
        }
    }

    public long getLongKey(String key) {
        logger.debug("Getting long key for: {}", key);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            long incrementedValue = jedis.incr(key);

            logger.info("Successfully incremented key: {}, new value: {}", key, incrementedValue);
            return incrementedValue;

        } catch (Exception e) {
            logger.error("Error incrementing long key: {}", key, e);
            return -1L;
        } finally {
            if (jedis != null) {
                try {
                    jedis.close();
                    logger.debug("Jedis connection closed for key: {}", key);
                } catch (Exception e) {
                    logger.warn("Error closing Jedis connection for key: {}", key, e);
                }
            }
        }
    }
}