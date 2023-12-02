package com.chen.blogbackend.entities;

public class App {
    String appName;
    String author;
    String introduction;

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
