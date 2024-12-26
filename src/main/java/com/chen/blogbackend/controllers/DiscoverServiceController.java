package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;

import com.chen.blogbackend.entities.Endpoint;
import com.chen.blogbackend.services.DiscoveryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import java.util.Map;

@Controller("/service")
public class DiscoverServiceController {

    @Autowired
    Jedis jedisClient;

    @Autowired
    DiscoveryService discoveryService;

    @GetMapping("/get_endpoint")
    @ResponseBody
    public String getEndpoint(HttpServletRequest request, @RequestParam Long userId, @RequestParam String serviceType) {
        //
        System.out.println(serviceType);
        System.out.println("notification_consumer");
        Endpoint endpoint = new Endpoint();
        //endpoint.setAddress(request.getRemoteAddr());
        if (serviceType.equals("notification_consumer")) {
            return JSON.toJSONString(discoveryService.queryNotificationConsumerService(userId));
        } else if (serviceType.equals("notification_producer")) {
            return JSON.toJSONString(discoveryService.queryNotificationProducerService(userId));
        } else {
            return "error";
        }
    }

    @PostMapping("/register_endpoint")
    @ResponseBody
    public String registerEndpoint(HttpServletRequest request, @RequestBody Map<String, Object> requestBody) {
        String serviceType = (String) requestBody.get("serviceType");
        Endpoint endpoint = new Endpoint();
        endpoint.setAddress(request.getRemoteAddr());
        System.out.println(serviceType);
        if (serviceType.equals("notification_consumer")) {
            return JSON.toJSONString(discoveryService.registerNotificationConsumerService(endpoint));
        } else if (serviceType.equals("notification_producer")) {
            return JSON.toJSONString(discoveryService.registerNotificationProducerService(endpoint));
        } else {
            return "error";
        }
    }



}
