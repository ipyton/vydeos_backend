package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.Date;
import java.util.Map;

@Entity
public class ChatGroup {
    String groupId;
    String userId;
    String groupName;
    String groupDescription;
    Date createDatetime;
    Map<String, String> config;
    String avatar;

    public ChatGroup(String groupId, String userId, String groupName, String groupDescription, Date createDatetime, Map<String, String> config, String avatar) {
        this.groupId = groupId;
        this.userId = userId;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.createDatetime = createDatetime;
        this.config = config;
        this.avatar = avatar;
    }

    public ChatGroup() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public Date getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
