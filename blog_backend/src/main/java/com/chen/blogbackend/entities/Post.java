package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.time.Instant;
import java.util.List;

@Entity
public class Post {
    //some frequently accessed objects such as likes or comments are in the redis.
    // The attributes here are providing partial information.
    private String authorID = "";
    private String authorName = "";
    private String postID = "";
    private Instant lastModified = null;
    private String content = "";
    private int likes = 0;
    private List<String> notice;
    private List<String> accessRules;
    private List<String> images;
    private List<String> voices;
    private List<String> videos;
    private List<String> comments;
    private String receiverId;
    private String location;

    public Post() {
    }

    public Post(String authorID, String authorName, String postID, Instant lastModified, String content, int likes, List<String> notice, List<String> accessRules, List<String> images, List<String> voices, List<String> videos, List<String> comments, String receiverId, String location) {
        this.authorID = authorID;
        this.authorName = authorName;
        this.postID = postID;
        this.lastModified = lastModified;
        this.content = content;
        this.likes = likes;
        this.notice = notice;
        this.accessRules = accessRules;
        this.images = images;
        this.voices = voices;
        this.videos = videos;
        this.comments = comments;
        this.receiverId = receiverId;
        this.location = location;
    }

    public Post(String authorID, String authorName, String postID, Instant lastModified, String content, int likes, List<String> notice, List<String> accessRules, List<String> images, List<String> voices, List<String> videos, List<String> comments, String location) {
        this.authorID = authorID;
        this.authorName = authorName;
        this.postID = postID;
        this.lastModified = lastModified;
        this.content = content;
        this.likes = likes;
        this.notice = notice;
        this.accessRules = accessRules;
        this.images = images;
        this.voices = voices;
        this.videos = videos;
        this.comments = comments;
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
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

    public List<String> getNotice() {
        return notice;
    }

    public void setNotice(List<String> notice) {
        this.notice = notice;
    }

    public List<String> getAccessRules() {
        return accessRules;
    }

    public void setAccessRules(List<String> accessRules) {
        this.accessRules = accessRules;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getVoices() {
        return voices;
    }

    public void setVoices(List<String> voices) {
        this.voices = voices;
    }

    public List<String> getVideos() {
        return videos;
    }

    public void setVideos(List<String> videos) {
        this.videos = videos;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    @Override
    public String toString() {
        return "Post{" +
                "authorID='" + authorID + '\'' +
                ", authorName='" + authorName + '\'' +
                ", postID='" + postID + '\'' +
                ", lastModified=" + lastModified +
                ", content='" + content + '\'' +
                ", likes=" + likes +
                ", notice=" + notice +
                ", accessRules=" + accessRules +
                ", images=" + images +
                ", voices=" + voices +
                ", videos=" + videos +
                ", comments=" + comments +
                ", receiverId='" + receiverId + '\'' +
                '}';
    }
}
