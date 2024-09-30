package com.chen.notification.controller;


import com.chen.notification.entities.Endpoints;
import com.chen.notification.service.EndpointQueryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

//This class is used for frontend customers getting the endpoint information.
@RestController
public class InformationEndpointRegistry {

    @Autowired
    EndpointQueryService queryService;

    @PostMapping("get_end_point")
    public Endpoints getEndPoint(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return queryService.queryEndpoint(userId);
    }

}
