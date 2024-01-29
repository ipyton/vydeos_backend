package com.chen.blogbackend.controllers;

import com.chen.blogbackend.entities.NotificationNegotiation;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    @MessageMapping("handshake")

    public NotificationNegotiation handShake(NotificationNegotiation negotiation) {

        return negotiation;
    }


}
