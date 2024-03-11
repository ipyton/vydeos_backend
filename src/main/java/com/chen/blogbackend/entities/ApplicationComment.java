package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.List;
import java.time.Instant;
@Entity
public class ApplicationComment {
    private String applicationId;
    private String commentId;
    private String userId;
    private String userName;
    private String userAvatar;
    private String comment;
    private float rate;
    private Instant commentDateTime;


    public ApplicationComment() {
    }

    public ApplicationComment(String applicationId, String userId, String comment, float rate, List<String> picture) {
        this.applicationId = applicationId;
        this.userId = userId;
        this.comment = comment;
        this.rate = rate;

    }

    public ApplicationComment(String applicationId, String userId, String comment, float rate) {
        this.applicationId = applicationId;
        this.userId = userId;
        this.comment = comment;
        this.rate = rate;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public float getRate() {
        return rate;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public Instant getCommentDateTime() {
        return commentDateTime;
    }

    public void setCommentDateTime(Instant commentDateTime) {
        this.commentDateTime = commentDateTime;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
}
