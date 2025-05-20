package com.chen.admin.services;

import com.chen.admin.assginStratergies.AssignStrategies;
import com.chen.admin.assginStratergies.NotificationAssignStrategies;
import com.datastax.oss.driver.api.core.CqlSession;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerPartitionAssignor;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Component
public class KafkaNotificationAssignor implements ConsumerPartitionAssignor {

    @Autowired
    ObservationServices observationServices;

    Map<String, AssignStrategies> assignStrategies = new HashMap<>();

    @PostConstruct
    public void init() {
        assignStrategies.put("SingleChat", new NotificationAssignStrategies());
        assignStrategies.put("GroupChat", new NotificationAssignStrategies());

    }

    @Override
    public GroupAssignment assign(Cluster metadata, GroupSubscription groupSubscription) {
        Set<String> topics = metadata.topics();
        for (String topic : topics) {
            List<PartitionInfo> partitionInfos = metadata.availablePartitionsForTopic(topic);
            List<String> hosts = new ArrayList<>();
            for (PartitionInfo partitionInfo : partitionInfos) {
                String host = partitionInfo.leader().host();
                hosts.add(host);
            }
            AssignStrategies assignStrategies1 = assignStrategies.get(topic);
            if (assignStrategies1 == null) {
                return null;
            }
            observationServices.getBrokersPerformance(hosts);


        }
        return null;
    }

    @Override
    public String name() {
        return "Custom Notification Assignor";
    }
}
