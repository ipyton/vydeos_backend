package com.chen.notification.service;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

public class NotificationListener implements ApplicationListener {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof SessionDisconnectEvent) {
            System.out.println("exit");
        }
    }
}
