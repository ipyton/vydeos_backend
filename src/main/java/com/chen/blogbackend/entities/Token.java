package com.chen.blogbackend.entities;

import java.time.Instant;
import java.util.Date;

public class Token {
    private String userId;
    private Instant expireDatetime;

    public String getTokenString() {
        return tokenString;
    }

    public void setTokenString(String tokenString) {
        this.tokenString = tokenString;
    }

    private String tokenString;


    public String getUserId() {
        return userId;
    }

    public Token(String userId, Instant expireDatetime, String tokenString) {
        this.userId = userId;
        this.tokenString = tokenString;
        this.expireDatetime = expireDatetime;
    }

    public void setUserId(String userEmail) {
        this.userId = userEmail;
    }


    public Instant getExpireDatetime() {
        return expireDatetime;
    }

    public void setExpireDatetime(Instant expireDatetime) {
        this.expireDatetime = expireDatetime;
    }
}
