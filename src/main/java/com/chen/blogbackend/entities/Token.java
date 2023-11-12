package com.chen.blogbackend.entities;

import java.util.Calendar;
import java.util.Date;

public class Token {
    private String userID;
    private String username;
    private Date expiresDateAndTime;

    public String getUserID() {
        return userID;
    }

    public Token(String userID, String username, int expiresSeconds) {
        this.userID = userID;
        this.username = username;
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, expiresSeconds);
        this.expiresDateAndTime = instance.getTime();
    }

    public Token(String userID, String username, Date expiresDateAndTime) {
        this.userID = userID;
        this.username = username;
        this.expiresDateAndTime = expiresDateAndTime;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getExpiresDateAndTime() {
        return expiresDateAndTime;
    }

    public void setExpiresDateAndTime(Date expiresDateAndTime) {
        this.expiresDateAndTime = expiresDateAndTime;
    }
}
