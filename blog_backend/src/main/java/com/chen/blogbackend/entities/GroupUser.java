package com.chen.blogbackend.entities;

//成员进入了哪些群组

public class GroupUser {
    private String userId;
    private long groupId;
    private String groupName;

    public GroupUser(String userId, long groupId, String groupName) {
        this.userId = userId;
        this.groupId = groupId;
        this.groupName = groupName;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    @Override
    public String toString() {
        return "GroupUser{" +
                "userId='" + userId + '\'' +
                ", groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
