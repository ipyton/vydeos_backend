package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.HashMap;

@Entity
public class Settings {
    String userId = "null";
    String applicationID = "3wjdikf";
    HashMap<String, String> map;

    public Settings() {
    }

    public Settings(String userId, String applicationID, HashMap<String, String> map) {
        this.userId = userId;
        this.applicationID = applicationID;
        this.map = map;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }


    public HashMap<String, String> getMap() {
        return map;
    }

    public void setMap(HashMap<String, String> map) {
        this.map = map;
    }
}
