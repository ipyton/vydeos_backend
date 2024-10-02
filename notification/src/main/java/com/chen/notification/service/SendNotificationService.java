package com.chen.notification.service;


import com.chen.notification.entities.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationService {

    @Autowired
    private SimpMessagingTemplate template;


    public void sendMessageToUser(String destination, Notification notification) {
        template.convertAndSendToUser(notification.getSenderName(), destination, notification);
    }

    public void broadCast(String destination, Notification notification) {
        template.convertAndSend(destination, notification);
    }


}
