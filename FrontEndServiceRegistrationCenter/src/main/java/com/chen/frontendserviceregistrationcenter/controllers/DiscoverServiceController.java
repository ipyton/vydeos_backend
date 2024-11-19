package com.chen.frontendserviceregistrationcenter.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.frontendserviceregistrationcenter.services.DiscoveryManagementService;
import com.chen.frontendserviceregistrationcenter.services.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

@Controller("/service")
public class DiscoverServiceController {

    @Autowired
    Jedis jedisClient;

    @Autowired
    DiscoveryService discoveryService;

    @GetMapping("/get_endpoint")
    @ResponseBody
    public String getEndpoint(@RequestParam String userId, @RequestParam String serviceType) {
        if (serviceType.equals("notification_receive_endpoint")) {
            return discoveryService.queryNotificationReceiverService(userId);
        } else if (serviceType.equals("notification_send_endpoint")) {
            return discoveryService.queryNotificationSenderService(userId);
        } else {
            return "";
        }
    }



}
