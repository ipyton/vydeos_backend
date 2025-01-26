package com.chen.notification.entities;

import jakarta.websocket.Session;

import java.security.Principal;

public class UserStatus implements Principal {
    private String userID;
    private long lastAck;
    private Session session;

    public void setLastAck(long lastAck) {
        this.lastAck = lastAck;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public UserStatus(String userID, long lastAck, Session session) {
        this.userID = userID;
        this.lastAck = lastAck;
        this.session = session;
    }

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
