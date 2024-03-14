package com.chen.notification.entities;


public class Negotiation {
    public String userID;
    public long time;
    public String method;

    public Negotiation(String userID, long time, String method) {
        this.userID = userID;
        this.time = time;
        this.method = method;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
