package com.chen.notification.endpoints;

import com.alibaba.fastjson.JSON;
import com.chen.notification.entities.Negotiation;
import com.chen.notification.entities.Notification;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/notification/{userId}")
public class NotificationServerEndpoint {

    private static final ConcurrentHashMap<String,Session> sessionPool = new ConcurrentHashMap<String,Session>();
    private final AtomicInteger integer = new AtomicInteger();
    private final AtomicInteger closedConnections = new AtomicInteger();

    // It is used for only ask for users.
    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<Notification>> messageList = new ConcurrentHashMap<>();

    // If user do not online, just abandon the function.

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        try {

            integer.incrementAndGet();
            if (integer.intValue()  > 10000) {
                System.out.println("add another machine.");
            }
            sessionPool.put(userId, session);
            if (!messageList.contains(userId)) {
                messageList.put(userId, new ConcurrentLinkedQueue<>());
            }
            System.out.println(userId);

            session.getBasicRemote().sendText("success");
            Thread thread = new Thread(()->{
                while(true) {
                    try {
                        session.getBasicRemote().sendText(JSON.toJSONString(new Notification("", "czhdawang@163.com", "AAAA1111", "chen", "text", "single", "hello", Instant.now())));
                        Thread.sleep(2000);
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }

            });
            //thread.start();
        } catch (Exception e) {
            System.out.println("fault");
        }
    }

    @OnClose
    public void onClose() {
        try {
            integer.decrementAndGet();
            closedConnections.incrementAndGet();
            if (closedConnections.intValue() > 3000) {
                System.out.println("collect the connections");
            }
        } catch (Exception e) {
        }
    }

    public void updateMessageList(String userId, Notification notification) {

        messageList.get(userId).add(notification);
    }

    private void sendMessage(String userId,Notification notification) throws IOException {
        Session session = sessionPool.get(userId);
        if (session.isOpen()) session.getBasicRemote().sendText(JSON.toJSONString(notification));
        else {
            sessionPool.remove(userId);
        }

    }

    // heart beat.
    @OnMessage
    public void handleMessage(String message, Session session) throws IOException {
        Negotiation negotiation = JSON.parseObject(message, Negotiation.class);
        String userName = negotiation.getUserID();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 5 && messageList.get(userName).size() > 0; i ++ ) {
            Notification notification = messageList.get(userName).poll();
            String s = JSON.toJSONString(notification);
            result.append(s);
            result.append(", ");
        }
        session.getBasicRemote().sendText("[" + result + "]");
    }
}
