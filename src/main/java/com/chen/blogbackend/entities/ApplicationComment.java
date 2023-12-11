package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

@Entity
public class ApplicationComment {
    String applicationId;
    String userId;
    String comment;
    float rate;

    public ApplicationComment() {
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

    public void setRate(float rate) {
        this.rate = rate;
    }
}
