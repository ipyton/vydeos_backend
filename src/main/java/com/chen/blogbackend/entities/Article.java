package com.chen.blogbackend.entities;

import java.util.Date;

public class Article {
    private String userID = "";
    private String articleID = "";
    private Date lastEdit = null;
    private String content = "";
    private int likes = 0;
    private int pictureAmount = 0;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getPictureAmount() {
        return pictureAmount;
    }

    public void setPictureAmount(int pictureAmount) {
        this.pictureAmount = pictureAmount;
    }
}
