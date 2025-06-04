package com.chen.notification.health;


import com.chen.notification.endpoints.NotificationServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class WebSocketHealthIndicator implements HealthIndicator {

    @Autowired
    private NotificationServerEndpoint notificationEndpoint;

    @Override
    public Health health() {
        try {
            int activeConnections = notificationEndpoint.getActiveConnectionsCount();
            int totalClosed = notificationEndpoint.getTotalClosedConnections();

            return Health.up()
                    .withDetail("activeConnections", activeConnections)
                    .withDetail("totalClosedConnections", totalClosed)
                    .withDetail("status", "WebSocket server is running")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}