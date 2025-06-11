package com.chen.blogbackend.entities;

//成员进入了哪些群组

public class GroupUser {
    private String userId;
    private Long groupId;
    private String groupName;
    private String userName;
    public GroupUser(String userId, Long groupId, String groupName, String userName) {
        this.userId = userId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.userName = userName;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
