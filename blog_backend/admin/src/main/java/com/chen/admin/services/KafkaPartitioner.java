package com.chen.admin.services;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

public class KafkaPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        Integer k = (Integer) key;
        k = k >> 16;
        int totalPartitions = cluster.partitionsForTopic(topic).size();
        if (k >= totalPartitions) {
            return -1;
        }
        return k;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
