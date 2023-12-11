package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Entity
public class App {

    String applicationId;
    String appName;
    String version;
    Date lastModified;
    float ratings;
    String type;
    HashMap<String, String> systemRequirements;
    ArrayList<String> historyVersions;
    ArrayList<String> pictures;
    ArrayList<String> hotComments;
    String author;
    String introduction;

    public String getAppId() {
        return applicationId;
    }

    public void setAppId(String appId) {
        this.applicationId = appId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public float getRatings() {
        return ratings;
    }

    public void setRatings(float ratings) {
        this.ratings = ratings;
    }

    public App() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HashMap<String, String> getSystemRequirements() {
        return systemRequirements;
    }

    public App(String appId, String appName, String version, Date lastModified, float ratings, String type, HashMap<String, String> systemRequirements, ArrayList<String> historyVersions, ArrayList<String> pictures, ArrayList<String> hotComments, String author, String introduction) {
        this.applicationId = appId;
        this.appName = appName;
        this.version = version;
        this.lastModified = lastModified;
        this.ratings = ratings;
        this.type = type;
        this.systemRequirements = systemRequirements;
        this.historyVersions = historyVersions;
        this.pictures = pictures;
        this.hotComments = hotComments;
        this.author = author;
        this.introduction = introduction;
    }

    public void setSystemRequirements(HashMap<String, String> systemRequirements) {
        this.systemRequirements = systemRequirements;
    }

    public ArrayList<String> getHistoryVersions() {
        return historyVersions;
    }

    public void setHistoryVersions(ArrayList<String> historyVersions) {
        this.historyVersions = historyVersions;
    }

    public ArrayList<String> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<String> pictures) {
        this.pictures = pictures;
    }

    public ArrayList<String> getHotComments() {
        return hotComments;
    }

    public void setHotComments(ArrayList<String> hotComments) {
        this.hotComments = hotComments;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public App(String appName, String author, String introduction) {
        this.appName = appName;
        this.author = author;
        this.introduction = introduction;
    }
}
