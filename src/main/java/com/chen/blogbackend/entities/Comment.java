package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.ArrayList;

@Entity
public class Comment {
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCommentDateTime() {
        return commentDateTime;
    }

    public void setCommentDateTime(String commentDateTime) {
        this.commentDateTime = commentDateTime;
    }

    String userId;

    String objectId;
    String commentId;
    String comment;
    int numberOfLikes;
    String commentDateTime;
    String type="";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Comment() {
    }

    public Comment(String userId, String objectId, String commentId, String comment, int numberOfLikes, String commentDateTime) {
        this.userId = userId;
        this.objectId = objectId;
        this.commentId = commentId;
        this.comment = comment;
        this.numberOfLikes = numberOfLikes;
        this.commentDateTime = commentDateTime;
    }




    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getComments() {
        return comment;
    }

    public void setComments(String comments) {
        this.comment = comments;
    }

    public int getNumberOfLikes() {
        return numberOfLikes;
    }

    public void setNumberOfLikes(int numberOfLikes) {
        this.numberOfLikes = numberOfLikes;
    }

    public String getCommentDateAndTime() {
        return commentDateTime;
    }

    public void setCommentDateAndTime(String commentDateAndTime) {
        this.commentDateTime = commentDateAndTime;
    }


}
