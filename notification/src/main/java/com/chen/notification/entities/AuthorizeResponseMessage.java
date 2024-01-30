package com.chen.notification.entities;

public class AuthorizeResponseMessage {
    private String status;
    private String userName;
    private String optional1;

    public String getStatus() {
        return status;
    }

    public AuthorizeResponseMessage() {
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOptional1() {
        return optional1;
    }

    public void setOptional1(String optional1) {
        this.optional1 = optional1;
    }
}
