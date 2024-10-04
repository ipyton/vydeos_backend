package com.chen.admin.adminServices;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@ResponseBody
public class ScaleService {

    private AdminClient adminClient;


    @PostConstruct
    public void init() {
        Properties props = new Properties();
        // this must should be controller's address.
        props.put(AdminClientConfig.BOOTSTRAP_CONTROLLERS_CONFIG, "localhost:9092");
        adminClient = AdminClient.create(props);
    }

    private Map<String, Integer> getPartitionsCounts(List<String> topicName) throws ExecutionException, InterruptedException {
        KafkaFuture<Map<String, TopicDescription>> mapKafkaFuture = adminClient.describeTopics(topicName).allTopicNames();
        Map<String, TopicDescription> stringTopicDescriptionMap = mapKafkaFuture.get();
        Map<String, Integer> topicCounts = new HashMap<>();
        for (Map.Entry<String, TopicDescription> stringTopicDescriptionEntry : stringTopicDescriptionMap.entrySet()) {
            topicCounts.put(stringTopicDescriptionEntry.getKey(), stringTopicDescriptionEntry.getValue().partitions().size());
        }
        return topicCounts;
    }

    public boolean scaleNotificationPartitions(List<String> topics) throws ExecutionException, InterruptedException {
        Map<String, Integer> partitionsCounts = getPartitionsCounts(topics);
        Map<String, NewPartitions> newPartitions = new HashMap<>();
        for (String topic : topics) {
            NewPartitions newPartition = NewPartitions.increaseTo((int) (partitionsCounts.get(topic) * 1.25));
            newPartitions.put(topic, newPartition);
        }
        adminClient.createPartitions(newPartitions);
        return true;
    }



}
