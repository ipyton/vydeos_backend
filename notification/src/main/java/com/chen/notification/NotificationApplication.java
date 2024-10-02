package com.chen.notification;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;


@SpringBootApplication
@EnableWebSocket
public class NotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }

}
