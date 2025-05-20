package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.time.Instant;
import java.util.Map;

@Entity
public class ChatGroup {
    long groupId;
    String groupName;
    String groupDescription;
    Instant createDatetime;
    Map<String, String> config;
    String avatar;
    String ownerId;

    public ChatGroup(long groupId, String groupName, String groupDescription, Instant createDatetime, String ownerId, Map<String, String> config, String avatar) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.createDatetime = createDatetime;
        this.config = config;
        this.avatar = avatar;
        this.ownerId = ownerId;
    }

    public ChatGroup() {
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
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

    public Instant getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(Instant createDatetime) {
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
