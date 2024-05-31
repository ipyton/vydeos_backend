package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
public class Post {

    private String userID = "";
    private String postID = "";
    private Date lastEdit = null;
    private String content = "";
    private int likes = 0;
    private int pictureAmount = 0;
    private List<String> users;
    private List<String> notice;
    private List<String> accessType;

    public Post(String userID, String postID, Date lastEdit, String content, int likes, int pictureAmount,
                List<String> users, String accessType, List<String> notice) {
        this.userID = userID;
        this.postID = postID;
        this.lastEdit = lastEdit;
        this.content = content;
        this.likes = likes;
        this.pictureAmount = pictureAmount;
        this.users = users;
        this.accessType = accessType;
        this.notice = notice;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public List<String> getNotice() {
        return notice;
    }

    public void setNotice(List<String> notice) {
        this.notice = notice;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public Post() {
    }

    public String getArticleID() {
        return postID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setArticleID(String articleID) {
        this.postID = articleID;
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
