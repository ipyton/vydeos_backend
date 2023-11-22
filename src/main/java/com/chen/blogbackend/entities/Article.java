package com.chen.blogbackend.entities;

import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.ArrayList;
import java.util.Date;

public class Article {
    String userID = "";
    String articleID = "";
    Date lastEdit = null;
    String url = "";
    int likes = 0;
    String commentUrl = "";
    ArrayList<String> pictureUrl;

    public String getArticleID() {
        return articleID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setArticleID(String articleID) {
        this.articleID = articleID;
    }

    public Date getLastEdit() {
        return lastEdit;
    }

    public void setLastEdit(Date lastEdit) {
        this.lastEdit = lastEdit;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getCommentUrl() {
        return commentUrl;
    }

    public void setCommentUrl(String commentUrl) {
        this.commentUrl = commentUrl;
    }

    public ArrayList<String> getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(ArrayList<String> pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
