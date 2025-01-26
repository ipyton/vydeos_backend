package com.chen.notification.endpoints;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chen.blogbackend.UserStatus;
import com.chen.notification.entities.Negotiation;
import com.chen.notification.entities.NotificationMessage;
import com.google.api.Http;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
@ServerEndpoint("/notification/{token}")
@Profile("endpoint")
public class NotificationServerEndpoint {

    private static final ConcurrentHashMap<String,Session> sessionPool = new ConcurrentHashMap();
    private final AtomicInteger integer = new AtomicInteger();
    private final AtomicInteger closedConnections = new AtomicInteger();

    // It is used for only ask for users.
    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<NotificationMessage>> messageList = new ConcurrentHashMap<>();
    private final String targetUrl = "http://localhost:8080/account/verifyToken";

    @Autowired
    private HttpClient httpClient;


    public NotificationServerEndpoint() {
    }

    // If user do not online, just abandon the function.

    @PostConstruct
    public void init() {
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
//                .usePlaintext()
//                .build();
//        httpClient = HttpClient.newHttpClient();
        System.out.println("init success" + httpClient);
        // Create a blocking stub
    }


    private String getUserId(String token) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(targetUrl))
                .header("Content-Type",  "application/json")
                .header("token", token)
                .POST(HttpRequest.BodyPublishers.ofString("{\"token\":\""+token+"\"}")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        JSONObject jsonObject = JSON.parseObject(response.body());
        return (String) jsonObject.get("message");
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException, InterruptedException {
        System.out.println(token);
        String userId = getUserId(token);
        try {
            sessionPool.put(userId, session);
            if (!messageList.contains(userId)) {
                messageList.put(userId, new ConcurrentLinkedQueue<>());
            }
            System.out.println(userId);

            session.getBasicRemote().sendText("success");
            //UserStatus.UserOnlineInformation online = UserStatus.UserOnlineInformation.newBuilder().setStatus("online").setUserId(userId).setLastReceivedTimestamp(timestamp).build();
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

    public <T> void sendMessages(List<NotificationMessage> messages) throws IOException {
        messages.forEach(message -> {
            String receiverId = message.getReceiverId();
            if (receiverId == null || receiverId.isEmpty()) {
                System.out.println("err : connection Not specified");
                return;
            }
            Session session = sessionPool.get(receiverId);
            if (session != null && session.isOpen()) {
                try{
                    System.out.println("send to receiver" + receiverId);
                    session.getBasicRemote().sendText(JSON.toJSONString(message));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                sessionPool.remove(receiverId);
            }

        });


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
