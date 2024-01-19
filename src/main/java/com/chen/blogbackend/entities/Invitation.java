package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.Date;

@Entity
public class Invitation {
    String groupId;
    Date expire_time;
    String userId;
    int limit;
    public Invitation() {
    }

    public Invitation(String groupId, Date expire_time, String userId, int limit) {
        this.groupId = groupId;
        this.expire_time = expire_time;
        this.userId = userId;
        this.limit = limit;
    }




    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Date getExpire_time() {
        return expire_time;
    }

    public void setExpire_time(Date expire_time) {
        this.expire_time = expire_time;
    }

    public String getGroupName() {
        return userId;
    }

    public void setGroupName(String groupName) {
        this.userId = groupName;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
