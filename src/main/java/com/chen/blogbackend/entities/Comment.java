package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.time.Instant;

@Entity
public class Comment {
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    private String userId;

    private String resourceId;
    private String commentId;
    private String content;
    private Long likes;
    private Instant time;
    private String type="";
    private String avatar="";
    private String userName="";

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Comment() {
    }

    public Comment(String userId, String resourceId, String type, String commentId, String content, Long likes, Instant commentDateTime) {
        this.userId = userId;
        this.type = type;
        this.resourceId = resourceId;
        this.commentId = commentId;
        this.content = content;
        this.likes = likes;
        this.time = commentDateTime;
    }



    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }


    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant commentDateAndTime) {
        this.time = commentDateAndTime;

    }



}
