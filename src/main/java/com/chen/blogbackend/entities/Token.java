package com.chen.blogbackend.entities;

import java.util.Calendar;
import java.util.Date;

public class Token {
    private String email;
    private Date expireDatetime;

    public String getTokenString() {
        return tokenString;
    }

    public void setTokenString(String tokenString) {
        this.tokenString = tokenString;
    }

    private String tokenString;


    public String getEmail() {
        return email;
    }

    public Token(String email, String tokenString, int expiresSeconds) {
        this.email = email;
        Calendar instance = Calendar.getInstance();
        this.tokenString = tokenString;
        instance.add(Calendar.SECOND, expiresSeconds);
        this.expireDatetime = instance.getTime();
    }

    public Token(String email, String tokenString, Date expireDatetime) {
        this.email = email;

        this.tokenString = tokenString;
        this.expireDatetime = expireDatetime;
    }

    public Token(String email, Date expireDatetime) {
        this.email = email;
        this.expireDatetime = expireDatetime;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public Date getExpireDatetime() {
        return expireDatetime;
    }

    public void setExpireDatetime(Date expireDatetime) {
        this.expireDatetime = expireDatetime;
    }
}
