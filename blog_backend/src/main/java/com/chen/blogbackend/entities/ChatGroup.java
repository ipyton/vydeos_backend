package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.time.Instant;
import java.util.Map;

@Entity
public class ChatGroup {
    long groupId;
    String groupName;
    String groupDescription;
    Instant createTime;
    Map<String, String> config;
    String avatar;
    String ownerId;
    Boolean allow_invite_by_token;

    public ChatGroup(long groupId, String groupName, String groupDescription,
                     Instant createTime, String ownerId, Map<String, String> config, String avatar, Boolean allow_invite_by_token) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.createTime = createTime;
        this.config = config;
        this.avatar = avatar;
        this.ownerId = ownerId;
        this.allow_invite_by_token = allow_invite_by_token;
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

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Boolean getAllow_invite_by_token() {
        return allow_invite_by_token;
    }

    public void setAllow_invite_by_token(Boolean allow_invite_by_token) {
        this.allow_invite_by_token = allow_invite_by_token;
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
