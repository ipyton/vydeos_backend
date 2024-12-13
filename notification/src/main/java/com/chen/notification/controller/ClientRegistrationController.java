package com.chen.notification.controller;

import com.chen.notification.entities.ActiveStatus;
import com.chen.notification.entities.RegistrationResponse;
import com.chen.notification.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.Jedis;

@Controller("notification")
public class ClientRegistrationController {

    @Autowired
    RegistrationService registrationService;

    @RequestMapping("/register")
    public RegistrationResponse registration(HttpServletRequest request) {
        String userId = request.getParameter("userId");
        return registrationService.register(userId);

    }

//    @RequestMapping("")
//    public ActiveStatus login() {
//
//    }




}
