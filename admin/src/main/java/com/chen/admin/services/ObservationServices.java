package com.chen.admin.services;

import com.chen.admin.entities.PerformanceMeter;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//this class is to get useful data for whole cluster for scaling.
// this is used ti
@Service
public class ObservationServices {

    @Autowired
    CqlSession session;

    public Map<String, PerformanceMeter> getBrokersPerformance(List<String> topics) {
        if (topics == null || topics.isEmpty()) {
            return null;
        }
        String url = "";
        RestTemplate template = new RestTemplate();
        Map<String, PerformanceMeter> map = new HashMap<>();
        for (String topic : topics) {
            String body = template.getForEntity(url + "/brokers", String.class).getBody();


        }

        return new HashMap<>();




    }


}
