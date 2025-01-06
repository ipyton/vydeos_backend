package com.chen.blogbackend.entities;

public class NotificationSubscription {
    private String endpoint;
    private String expirationTime; // Use String for nullability; can be converted to LocalDateTime if needed.
    private Keys keys;

    // Nested class for keys
    public static class Keys {
        private String p256dh;
        private String auth;

        // Getters and Setters
        public String getP256dh() {
            return p256dh;
        }

        public void setP256dh(String p256dh) {
            this.p256dh = p256dh;
        }

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }

        @Override
        public String toString() {
            return "Keys{" +
                    "p256dh='" + p256dh + '\'' +
                    ", auth='" + auth + '\'' +
                    '}';
        }
    }

    // Getters and Setters
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Keys getKeys() {
        return keys;
    }

    public void setKeys(Keys keys) {
        this.keys = keys;
    }

    @Override
    public String toString() {
        return "NotificationEndpoint{" +
                "endpoint='" + endpoint + '\'' +
                ", expirationTime='" + expirationTime + '\'' +
                ", keys=" + (keys != null ? keys.toString() : "null") +
                '}';
    }
}
