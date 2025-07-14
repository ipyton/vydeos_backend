package com.chen.blogbackend.entities;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Entity class representing an invitation record from chat.invitations table
 */
public class Invitation {
    private String type;
    private Long groupId;
    private String userId;
    private Instant expireTime;
    private String code;
    private Instant createTime;

    // Default constructor
    public Invitation() {}

    // Constructor with all fields
    public Invitation(String type, Long groupId, String userId, Instant expireTime, String code, Instant createTime) {
        this.type = type;
        this.groupId = groupId;
        this.userId = userId;
        this.expireTime = expireTime;
        this.code = code;
        this.createTime = createTime;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "Invitation{" +
                "type='" + type + '\'' +
                ", groupId=" + groupId +
                ", userId='" + userId + '\'' +
                ", expireTime=" + expireTime +
                ", code='" + code + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }

    // equals and hashCode based on primary key (type, groupId, userId)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invitation that = (Invitation) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, groupId, userId);
    }
}