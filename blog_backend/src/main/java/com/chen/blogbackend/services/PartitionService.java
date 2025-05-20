package com.chen.blogbackend.services;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewPartitions;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;


// This service is to use
@Service
public class PartitionService {
    public void addPartition(){
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // 替换为你的 Kafka broker 地址
        try (AdminClient adminClient = AdminClient.create(props)) {

            // 设置主题及新的分区数
            String topicName = "your_topic_name";
            int newPartitionCount = 6; // 新的分区数量

            // 创建分区扩容请求
            Map<String, NewPartitions> newPartitions = Collections.singletonMap(
                    topicName, NewPartitions.increaseTo(newPartitionCount)
            );

            // 执行分区扩容
            adminClient.createPartitions(newPartitions).all().get();

            System.out.println("分区扩容完成，主题 " + topicName + " 现在有 " + newPartitionCount + " 个分区。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reBalance() {}


}
