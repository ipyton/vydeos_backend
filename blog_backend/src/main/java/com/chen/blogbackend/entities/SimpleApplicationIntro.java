package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.ArrayList;


public class SimpleApplicationIntro {
    String applicationID;
    String name;
    String intro;
    ArrayList<String> pictures;

    public SimpleApplicationIntro() {
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public ArrayList<String> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<String> pictures) {
        this.pictures = pictures;
    }

    public SimpleApplicationIntro(String applicationID, String name, String intro, ArrayList<String> pictures) {
        this.applicationID = applicationID;
        this.name = name;
        this.intro = intro;
        this.pictures = pictures;
    }
}
