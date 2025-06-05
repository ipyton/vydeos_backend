package com.chen.notification.endpoints;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chen.notification.DecodersAndEncoders.MessageDecoder;
import com.chen.notification.DecodersAndEncoders.MessageEncoder;
import com.chen.notification.configs.WebSocketConfig;
import com.chen.notification.entities.Negotiation;
import com.chen.notification.entities.NotificationMessage;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Component
@ServerEndpoint(value = "/notification/{token}",
        configurator = WebSocketConfig.class,  // Add this for Spring integration
        decoders = {MessageDecoder.class},     // Optional: for message decoding
        encoders = {MessageEncoder.class})     // Optional: for message encoding
@Profile("dispatcher")
public class NotificationServerEndpoint {

    private static final Logger logger = Logger.getLogger(NotificationServerEndpoint.class.getName());
    private static final ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, String> sessionToUserMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, Long> sessionHeartbeat = new ConcurrentHashMap<>();

    private final AtomicInteger activeConnections = new AtomicInteger();
    private final AtomicInteger closedConnections = new AtomicInteger();

    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<NotificationMessage>> messageList = new ConcurrentHashMap<>();
    private final String targetUrl = "http://localhost:8080/account/verifyToken";

    // Use a shared HttpClient with proper configuration
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Heartbeat scheduler
    private final ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);

    public NotificationServerEndpoint() {
        // Start heartbeat checker
        startHeartbeatChecker();
    }

    @PostConstruct
    public void init() {
        logger.info("NotificationServerEndpoint initialized successfully");
    }

    private void startHeartbeatChecker() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            sessionHeartbeat.entrySet().removeIf(entry -> {
                Session session = entry.getKey();
                long lastHeartbeat = entry.getValue();

                // If no heartbeat for 60 seconds, close the connection
                if (currentTime - lastHeartbeat > 60000) {
                    try {
                        if (session.isOpen()) {
                            session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Heartbeat timeout"));
                        }
                    } catch (IOException e) {
                        logger.warning("Error closing session due to heartbeat timeout: " + e.getMessage());
                    }
                    return true; // Remove from map
                }
                return false;
            });
        }, 30, 30, TimeUnit.SECONDS);
    }

    private String getUserId(String token) throws IOException, InterruptedException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("token", token)
                    .POST(HttpRequest.BodyPublishers.ofString("{\"token\":\"" + token + "\"}"))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Token verification failed with status: " + response.statusCode());
            }

            JSONObject jsonObject = JSON.parseObject(response.body());
            Object message = jsonObject.get("message");

            if (message == null) {
                throw new IOException("Invalid token verification response");
            }

            return message.toString();
        } catch (Exception e) {
            logger.severe("Token verification failed: " + e.getMessage());
            throw e;
        }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            logger.info("WebSocket connection attempt with token: " + token);

            // Configure session
            session.setMaxIdleTimeout(300000); // 5 minutes
            session.setMaxTextMessageBufferSize(8192);
            session.setMaxBinaryMessageBufferSize(8192);

            String userId = getUserId(token);

            // Store session mappings
            sessionPool.put(userId, session);
            sessionToUserMap.put(session, userId);
            sessionHeartbeat.put(session, System.currentTimeMillis());

            // Initialize message queue for user
            messageList.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>());

            logger.info("User connected successfully: " + userId);

            // Send connection success response
            if (session.isOpen()) {
                session.getBasicRemote().sendText("success");
            }

            activeConnections.incrementAndGet();

            // Send any pending messages
            sendPendingMessages(userId, session);

        } catch (Exception e) {
            logger.severe("Error in onOpen: " + e.getMessage());
            cleanupSession(session);
            try {
                if (session.isOpen()) {
                    session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Authentication failed"));
                }
            } catch (IOException ioException) {
                logger.severe("Error closing session after authentication failure: " + ioException.getMessage());
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        try {
            String userId = sessionToUserMap.get(session);
            logger.info("WebSocket connection closed for user: " + userId +
                    ", Reason: " + closeReason.getReasonPhrase() +
                    ", Code: " + closeReason.getCloseCode());

            cleanupSession(session);
            activeConnections.decrementAndGet();
            closedConnections.incrementAndGet();

            if (closedConnections.get() % 100 == 0) {
                logger.info("Connection statistics - Active: " + activeConnections.get() +
                        ", Total closed: " + closedConnections.get());
            }

        } catch (Exception e) {
            logger.severe("Error in onClose: " + e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        String userId = sessionToUserMap.get(session);

        // Handle different types of exceptions appropriately
        if (throwable instanceof java.io.EOFException) {
            logger.info("Client disconnected unexpectedly (EOFException) for user: " + userId);
        } else if (throwable instanceof IOException) {
            logger.info("IO error for user " + userId + ": " + throwable.getMessage());
        } else if (throwable instanceof java.util.concurrent.TimeoutException) {
            logger.info("Connection timeout for user: " + userId);
        } else {
            logger.warning("WebSocket error for user " + userId + ": " + throwable.getMessage());
        }

        // Clean up the session
        cleanupSession(session);

        try {
            if (session.isOpen()) {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Connection error"));
            }
        } catch (IOException e) {
            logger.fine("Error closing session after error (expected): " + e.getMessage());
        }
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        try {
            // Update heartbeat
            sessionHeartbeat.put(session, System.currentTimeMillis());

            // Handle heartbeat/ping messages
            if ("ping".equals(message.trim())) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText("pong");
                }
                return;
            }

            // Parse the negotiation message
            Negotiation negotiation = JSON.parseObject(message, Negotiation.class);
            String userName = negotiation.getUserID();

            if (userName == null || userName.trim().isEmpty()) {
                logger.warning("Received message with empty userName");
                return;
            }

            // Get messages for the user
            List<String> messageResults = new ArrayList<>();
            ConcurrentLinkedQueue<NotificationMessage> userMessages = messageList.get(userName);

            if (userMessages != null) {
                // Get up to 5 messages
                for (int i = 0; i < userMessages.size() && !userMessages.isEmpty(); i++) {
                    NotificationMessage notificationMessage = userMessages.poll();
                    if (notificationMessage != null) {
                        messageResults.add(JSON.toJSONString(notificationMessage));
                    }
                }
            }

            // Send response
            if (session.isOpen()) {
                System.out.println("send web push message");
                String response = "[" + String.join(",", messageResults) + "]";
                session.getBasicRemote().sendText(response);
            }

        } catch (Exception e) {
            logger.severe("Error handling message from session: " + e.getMessage());
            // Don't call onError here as it may cause infinite loop
            cleanupSession(session);
        }
    }

    private void sendPendingMessages(String userId, Session session) {
        try {
            ConcurrentLinkedQueue<NotificationMessage> userMessages = messageList.get(userId);
            if (userMessages != null && !userMessages.isEmpty() && session.isOpen()) {
                // Send a few pending messages immediately
                List<String> pendingMessages = new ArrayList<>();
                for (int i = 0; i < 15 && !userMessages.isEmpty(); i++) {
                    NotificationMessage message = userMessages.poll();
                    if (message != null) {
                        pendingMessages.add(JSON.toJSONString(message));
                    }
                }

                if (!pendingMessages.isEmpty()) {
                    String response = "[" + String.join(",", pendingMessages) + "]";
                    session.getBasicRemote().sendText(response);
                }
            }
        } catch (IOException e) {
            logger.warning("Error sending pending messages: " + e.getMessage());
        }
    }

    private void cleanupSession(Session session) {
        if (session != null) {
            String userId = sessionToUserMap.remove(session);
            if (userId != null) {
                sessionPool.remove(userId);
                logger.fine("Cleaned up session for user: " + userId);
            }
            sessionHeartbeat.remove(session);
        }
    }

    public void updateMessageList(String userId, NotificationMessage notificationMessage) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.warning("Cannot update message list: userId is null or empty");
            return;
        }

        ConcurrentLinkedQueue<NotificationMessage> queue = messageList.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>());
        queue.add(notificationMessage);

        // Limit queue size to prevent memory issues
        while (queue.size() > 100) {
            queue.poll();
        }
    }

    public void sendMessages(List<NotificationMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            System.out.println("is empty");
            return;
        }

        messages.parallelStream().forEach(message -> {
            String receiverId = message.getReceiverId();
            if (receiverId == null || receiverId.trim().isEmpty()) {
                logger.warning("Message has empty receiverId, skipping");
                return;
            }
            System.out.println("receiverId----=====" + receiverId);
            Session session = sessionPool.get(receiverId);
            if (session != null && session.isOpen()) {
                try {
                    String jsonMessage = JSON.toJSONString(message);
                    session.getBasicRemote().sendText(jsonMessage);
                    logger.fine("Message sent to user: " + receiverId);
                } catch (IOException e) {
                    logger.warning("Failed to send message to " + receiverId + ": " + e.getMessage());
                    cleanupSession(session);
                    System.out.println("Failed to send message to " + receiverId + ": " + e.getMessage());
                    // Store message for later delivery
                    //updateMessageList(receiverId, message);
                }
            } else {
                // Clean up invalid sessions
                if (session != null) {
                    cleanupSession(session);
                }
                System.out.println("Failed to send message to " + receiverId + ", deprecated");

                // Store message for later delivery
                //updateMessageList(receiverId, message);
                //logger.fine("User " + receiverId + " is offline, message queued");
            }
        });
    }

    // Utility methods
    public boolean isUserOnline(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        Session session = sessionPool.get(userId);
        return session != null && session.isOpen();
    }

    public int getActiveConnectionsCount() {
        return activeConnections.get();
    }

    public int getTotalClosedConnections() {
        return closedConnections.get();
    }

    public int getQueuedMessagesCount(String userId) {
        ConcurrentLinkedQueue<NotificationMessage> queue = messageList.get(userId);
        return queue != null ? queue.size() : 0;
    }

    // Cleanup method to be called on application shutdown
    public void shutdown() {
        heartbeatScheduler.shutdown();
        try {
            if (!heartbeatScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                heartbeatScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            heartbeatScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}