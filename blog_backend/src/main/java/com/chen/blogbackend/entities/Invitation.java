package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.time.Instant;

@Entity
public class Invitation {
    private int invitationId;
    private String targetType;
    private String targetId;
    private Long groupId;
    private Instant expireTime;
    private String senderId ;
    private String userId;
    private String token;
    private int limit;
    private Instant inviteTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Invitation(int invitationId, String targetType, String targetId, Instant expire_time, String senderId, String token, int limit, long groupId, Instant inviteTime) {
        this.invitationId = invitationId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.expireTime = expire_time;
        this.senderId = senderId;
        this.token = token;
        this.limit = limit;
        this.inviteTime = inviteTime;
        this.groupId = groupId;
    }

    public int getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(int invitationId) {
        this.invitationId = invitationId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Invitation() {
    }



    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public Instant getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
    }

    public Instant getInviteTime() {
        return inviteTime;
    }

    public void setInviteTime(Instant inviteTime) {
        this.inviteTime = inviteTime;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }



    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }






}
