package com.chen.notification.entities;

public class AuthorizeRequestMessage {
    private String status;
    private String token;
    private String requestService;

    public AuthorizeRequestMessage() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRequestService() {
        return requestService;
    }

    public void setRequestService(String requestService) {
        this.requestService = requestService;
    }
}
