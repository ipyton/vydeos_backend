package com.chen.blogbackend.entities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;


public class Account {

    private String userId = "";
    private String userEmail ="";
    private String userName="";
    private String password="";
    private String introduction="";
    private String avatar="";
    private LocalDate dateOfBirth = null;
    private String telephone="";
    private boolean gender = false;
    List<String> apps;

    public Account(String userId, String userEmail, String password, String telephone) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.password = password;
        this.telephone = telephone;
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

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public void setApps(List<String> apps) {
        this.apps = apps;
    }

    public Account(String userEmail, String userName, String password, String introduction, String avatar, LocalDate dateOfBirth, String telephone, List<String> apps) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.password = password;
        this.introduction = introduction;
        this.avatar = avatar;
        this.dateOfBirth = dateOfBirth;
        this.telephone = telephone;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    @Override
    public String toString() {
        return "Account{" +
                "userID='" + userEmail + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", introduction='" + introduction + '\'' +
                ", avatar='" + avatar + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
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
}
