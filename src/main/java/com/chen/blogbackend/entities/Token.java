package com.chen.blogbackend.entities;

import java.util.Calendar;
import java.util.Date;

public class Token {
    private String email;
    private Date expiresDateAndTime;

    public String getEmail() {
        return email;
    }

    public Token(String email, int expiresSeconds) {
        this.email = email;

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, expiresSeconds);
        this.expiresDateAndTime = instance.getTime();
    }

    public Token(String email, Date expiresDateAndTime) {
        this.email = email;

        this.expiresDateAndTime = expiresDateAndTime;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public Date getExpiresDateAndTime() {
        return expiresDateAndTime;
    }

    public void setExpiresDateAndTime(Date expiresDateAndTime) {
        this.expiresDateAndTime = expiresDateAndTime;
    }
}
