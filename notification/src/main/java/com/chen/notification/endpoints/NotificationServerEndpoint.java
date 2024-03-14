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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/notification/{userId}")
public class NotificationServerEndpoint {


    private static ConcurrentHashMap<String,Session> sessionPool = new ConcurrentHashMap<String,Session>();
    private final AtomicInteger integer = new AtomicInteger();
    private final AtomicInteger closedConnections = new AtomicInteger();
    public  static ConcurrentHashMap<String, ConcurrentLinkedQueue<Notification>> messageList = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        try {
            integer.incrementAndGet();
            if (integer.intValue()  > 10000) {
                System.out.println("add another machine.");
            }
            sessionPool.put(userId, session);
            if (!messageList.contains(userId)) {
                messageList.put(userId, new ConcurrentLinkedQueue<Notification>());
            }
            session.getBasicRemote().sendText("success");
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


    private void refresh() {

    }


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
