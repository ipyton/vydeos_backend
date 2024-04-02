package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.time.LocalDate;

@Entity
public class Friend {
    String userId = "";
    String subjectId="";
    String avatar = "";
    String introduction = "";
    String name = "";
    String groupId = "";
    int relationship=0;
    String location = "";
    private LocalDate dateOfBirth= null;


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public int getRelationship() {
        return relationship;
    }

    public void setRelationship(int relationship) {
        this.relationship = relationship;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getGroupId() {
        return groupId;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "userId='" + userId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", avatar='" + avatar + '\'' +
                ", introduction='" + introduction + '\'' +
                ", name='" + name + '\'' +
                ", groupId='" + groupId + '\'' +
                ", relationship=" + relationship +
                ", location='" + location + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }

    public Friend(String userId, String subjectId, String avatar, String introduction, String name, String groupId, int relationship, String location, LocalDate dateOfBirth) {
        this.userId = userId;
        this.subjectId = subjectId;
        this.avatar = avatar;
        this.introduction = introduction;
        this.name = name;
        this.groupId = groupId;
        this.relationship = relationship;
        this.location = location;
        this.dateOfBirth = dateOfBirth;
    }

    public Friend(String userId, String avatar, String introduction, String groupId, int relationship) {
        this.userId = userId;
        this.avatar = avatar;
        this.introduction = introduction;
        this.groupId = groupId;
        this.relationship = relationship;

    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }



    public String getUserId() {
        return userId;
    }

    public Friend() {
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }


    public Friend(String userId, String avatar, String introduction, String[] apps) {
        this.userId = userId;
        this.avatar = avatar;
        this.introduction = introduction;
    }


}
