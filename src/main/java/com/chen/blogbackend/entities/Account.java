package com.chen.blogbackend.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


// This is a universal entity to transport user(friend details, owner detail)
public class Account {

    private String userId = "";
    private String userEmail ="";
    private String userName="";
    private String introduction="";
    private String avatar="";
    private LocalDate dateOfBirth = null;
    private String telephone="";
    private boolean gender = false;
    private int relationship = 20;
    private String group = "";
    private String location = "";
    private String language = "";
    private String country = "";


    public Account(String userId, String userEmail, String userName, String introduction, String avatar,
                   LocalDate dateOfBirth, String telephone, boolean gender, int relationship, String group,
                   String location, String language, String country, List<String> apps) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.introduction = introduction;
        this.avatar = avatar;
        this.dateOfBirth = dateOfBirth;
        this.telephone = telephone;
        this.gender = gender;
        this.relationship = relationship;
        this.group = group;
        this.location = location;
        this.language = language;
        this.country = country;
        this.apps = apps;
    }

    List<String> apps = new ArrayList<>();

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isGender() {
        return gender;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getRelationship() {
        return relationship;
    }

    public void setRelationship(int relationship) {
        this.relationship = relationship;
    }




    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public List<String> getApps() {
        return apps;
    }

    public Account() {

    }

    public Account(String userId, List<String> apps, String avatar, LocalDate birthDate,
                   boolean gender, String intro, String userName) {
        this.userId = userId;
        this.apps = apps;
        this.avatar = avatar;
        this.dateOfBirth = birthDate;
        this.gender = gender;
        this.introduction = intro;
        this.userName = userName;

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean getGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public void setApps(List<String> apps) {
        this.apps = apps;
    }



    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public String toString() {
        return "Account{" +
                "userId='" + userId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userName='" + userName + '\'' +
                ", introduction='" + introduction + '\'' +
                ", avatar='" + avatar + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", telephone='" + telephone + '\'' +
                ", gender=" + gender +
                ", relationship=" + relationship +
                ", group='" + group + '\'' +
                ", apps=" + apps +
                '}';
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
