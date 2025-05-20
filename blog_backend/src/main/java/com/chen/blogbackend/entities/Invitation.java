package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.Date;

@Entity
public class Invitation {
    private int invitation_id;
    private String receiverId;
    private String targetId;
    private Date expire_time;
    private String senderId ;
    private String message;
    private int limit;
    public Invitation() {
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public Date getExpire_time() {
        return expire_time;
    }

    public void setExpire_time(Date expire_time) {
        this.expire_time = expire_time;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Invitation(String receiverId, Date expire_time, String userId, int limit) {
        this.receiverId = receiverId;
        this.expire_time = expire_time;
        this.senderId = userId;
        this.limit = limit;
    }




}
