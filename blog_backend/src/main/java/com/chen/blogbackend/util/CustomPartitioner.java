package com.chen.blogbackend.util;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

public class CustomPartitioner implements Partitioner {

    @Override
    public void configure(Map<String, ?> configs) {
        // 可选：从配置中获取初始化参数
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // 获取主题的分区数
        int numPartitions = cluster.partitionCountForTopic(topic);

        // 自定义分区逻辑
        if (key == null) {
            // 如果没有 key，默认分区（例如分区 0）
            return 0;
        } else if (key.toString().startsWith("priority")) {
            // 如果 key 以 "priority" 开头，发送到分区 0
            return 0;
        } else {
            // 根据 key 的 hash 值分区
            return Math.abs(key.hashCode()) % numPartitions;
        }
    }

    @Override
    public void close() {
        // 可选：清理资源
    }
}
