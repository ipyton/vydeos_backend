package com.chen.blogbackend.entities;

import java.util.Date;

public class Token {
    private String userEmail;
    private Date expireDatetime;

    public String getTokenString() {
        return tokenString;
    }

    public void setTokenString(String tokenString) {
        this.tokenString = tokenString;
    }

    private String tokenString;


    public String getUserEmail() {
        return userEmail;
    }



    public Token(String userEmail, Date expireDatetime, String tokenString) {
        this.userEmail = userEmail;
        this.tokenString = tokenString;
        this.expireDatetime = expireDatetime;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }


    public Date getExpireDatetime() {
        return expireDatetime;
    }

    public void setExpireDatetime(Date expireDatetime) {
        this.expireDatetime = expireDatetime;
    }
}
