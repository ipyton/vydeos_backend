package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.HashMap;

@Entity
public class Setting {
    String name = "null";
    String applicationID = "3wjdikf";
    HashMap<String, String> map;

    public Setting() {
    }

    public Setting(String name, String applicationID, HashMap<String, String> map) {
        this.name = name;
        this.applicationID = applicationID;
        this.map = map;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
