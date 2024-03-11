package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.time.Instant;
import java.util.*;

@Entity
public class App {

    String applicationId;
    String appName;
    String version;
    Instant lastModified;
    float ratings;
    String type;
    Map<String, String> systemRequirements;
    List<String> historyVersions;
    List<String> pictures;
    List<String> hotComments;
    String author;
    String introduction;

    public App() {
    }



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

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    public float getRatings() {
        return ratings;
    }

    public void setRatings(float ratings) {
        this.ratings = ratings;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getSystemRequirements() {
        return systemRequirements;
    }

    public App(String appId, String appName, String version, Instant lastModified, float ratings, String type, Map<String, String> systemRequirements, List<String> historyVersions, List<String> pictures, List<String> hotComments, String author, String introduction) {
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



    public App(String applicationId, String appName, float ratings, List<String> pictures, String author) {
        this.applicationId = applicationId;
        this.appName = appName;
        this.ratings = ratings;
        this.pictures = pictures;
        this.author = author;
    }

    public void setSystemRequirements(HashMap<String, String> systemRequirements) {
        this.systemRequirements = systemRequirements;
    }

    public List<String> getHistoryVersions() {
        return historyVersions;
    }

    public void setHistoryVersions(ArrayList<String> historyVersions) {
        this.historyVersions = historyVersions;
    }

    public List<String> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<String> pictures) {
        this.pictures = pictures;
    }

    public List<String> getHotComments() {
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
