package com.chen.blogbackend.entities;

public class Comment {
    String ArticleId;
    String comments;
    int numberOfLikes;
    String commentDateAndTime;


    public String getArticleId() {
        return ArticleId;
    }

    public void setArticleId(String articleId) {
        ArticleId = articleId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getNumberOfLikes() {
        return numberOfLikes;
    }

    public void setNumberOfLikes(int numberOfLikes) {
        this.numberOfLikes = numberOfLikes;
    }

    public String getCommentDateAndTime() {
        return commentDateAndTime;
    }

    public void setCommentDateAndTime(String commentDateAndTime) {
        this.commentDateAndTime = commentDateAndTime;
    }


}
