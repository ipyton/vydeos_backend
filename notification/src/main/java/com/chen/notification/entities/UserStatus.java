package com.chen.notification.entities;

import java.security.Principal;

public class UserStatus implements Principal {
    private String userID;
    private long lastAck;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public long getLastAck() {
        return lastAck;
    }

    public void setLastAck(int lastAck) {
        this.lastAck = lastAck;
    }

    public UserStatus(String userID, int lastAck) {
        this.userID = userID;
        this.lastAck = lastAck;
    }

    public UserStatus(String userID) {
        this.userID = userID;
        this.lastAck = System.currentTimeMillis();
    }
    public UserStatus() {
    }

    @Override
    public String getName() {
        return userID;
    }
}
