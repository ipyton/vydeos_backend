package com.chen.admin.adminServices;

import org.apache.kafka.clients.consumer.ConsumerPartitionAssignor;
import org.apache.kafka.common.Cluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class KafkaNotificationAssignor implements ConsumerPartitionAssignor {

    @Autowired


    @Override
    public GroupAssignment assign(Cluster metadata, GroupSubscription groupSubscription) {
        RestTemplate restTemplate = new RestTemplate();

        return null;
    }

    @Override
    public String name() {
        return "Custom Notification Assignor";
    }
}
