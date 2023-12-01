package com.chen.blogbackend.entities.Comment;

public class SubComment {
    String commentId;
    String commentIdToComment;
    String comment;
    String commentDateAndTime;
    int numberOfLikes;


    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentIdToComment() {
        return commentIdToComment;
    }

    public void setCommentIdToComment(String commentIdToComment) {
        this.commentIdToComment = commentIdToComment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
