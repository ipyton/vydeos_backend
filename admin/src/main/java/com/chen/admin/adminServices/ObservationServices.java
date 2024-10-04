package com.chen.admin.adminServices;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//this class is to get useful data for whole cluster for scaling.
@Service
public class ObservationServices {

    @Autowired
    CqlSession session;

    public Map<String, > getBrokersPerformance(String topic) {
        for
    }


}
