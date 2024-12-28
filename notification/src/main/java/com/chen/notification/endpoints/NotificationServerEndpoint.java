package com.chen.notification.endpoints;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.UserStatus;
import com.chen.notification.entities.Negotiation;
import com.chen.notification.entities.NotificationMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


// situations for sending notification.
// 1. only push when the user is online status
// 2. the user: online-> offline
// | discard the unconnected receiver's notifications.
// | when the user comes to online, the system check the unread information to send.
@Component
@ServerEndpoint("/notification/{userId}")
@Profile("endpoint")
public class NotificationServerEndpoint {

    private static final ConcurrentHashMap<Long,Session> sessionPool = new ConcurrentHashMap();
    private final AtomicInteger integer = new AtomicInteger();
    private final AtomicInteger closedConnections = new AtomicInteger();

    // It is used for only ask for users.
    public static ConcurrentHashMap<Long, ConcurrentLinkedQueue<NotificationMessage>> messageList = new ConcurrentHashMap<>();

    // If user do not online, just abandon the function.

    @PostConstruct
    public void init() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        // Create a blocking stub
    }



    @OnOpen
    public void onOpen(Session session, @PathParam("userId") long userId, @Param("latest_timestamp") long timestamp) {
        try {

            sessionPool.put(userId, session);
            if (!messageList.contains(userId)) {
                messageList.put(userId, new ConcurrentLinkedQueue<>());
            }
            System.out.println(userId);

            session.getBasicRemote().sendText("success");
            UserStatus.UserOnlineInformation online = UserStatus.UserOnlineInformation.newBuilder().setStatus("online").setUserId(userId).setLastReceivedTimestamp(timestamp).build();
//            UserStatus.MessageResponse messageResponse = stub.onlineHandler(online);
            List<NotificationMessage> notificationMessageList = new ArrayList<>();
//            sendMessages(messageResponse.getGroupMessagesList(), messageResponse.getUserId());
            
//            Thread thread = new Thread(()->{
//                while(true) {
//                    try {
//                        session.getBasicRemote().sendText(JSON.toJSONString(new Notification("", "czhdawang@163.com", "AAAA1111", "chen", "text", "single", "hello", Instant.now())));
//                        Thread.sleep(2000);
//                    } catch (InterruptedException | IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            });
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

    public void updateMessageList(String userId, NotificationMessage notificationMessage) {
        messageList.get(userId).add(notificationMessage);
    }

    public <T> void sendMessages( List<T> messages, long userId) throws IOException {

        Session session = sessionPool.get(userId);
        if (session.isOpen() && messages.size() > 0) {
            session.getBasicRemote().sendText(JSON.toJSONString(messages));
//            if (messages.get(0) instanceof SingleMessage) {
//                List<SingleMessage> list = new ArrayList<>();
//                for (Object singleMessage : messages) {
//                    SingleMessage singleMessage1 = (SingleMessage) singleMessage;
//                    list.add(singleMessage1);
//                }
//                session.getBasicRemote().sendText(JSON.toJSONString(list));
//            }
//            else if (messages.get(0) instanceof GroupMessage) {
//                List<GroupMessage> list = new ArrayList<>();
//                for (Object singleMessage : messages) {
//                    GroupMessage groupMessage1 = (GroupMessage) singleMessage;
//                    list.add(groupMessage1);
//                }
//                session.getBasicRemote().sendText(JSON.toJSONString(list));
//            }

            System.out.println("successfully");}

        else {
            System.out.println("user" + userId + "do not exist!!!!!!");
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
            NotificationMessage notificationMessage = messageList.get(userName).poll();
            String s = JSON.toJSONString(notificationMessage);
            result.append(s);
            result.append(", ");
        }
        session.getBasicRemote().sendText("[" + result + "]");
    }
}
