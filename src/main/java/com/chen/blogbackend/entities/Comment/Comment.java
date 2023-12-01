package com.chen.blogbackend.entities.Comment;

import java.util.ArrayList;

public class Comment {
    String objectId;
    String commentId;
    String comment;
    int numberOfLikes;
    String commentDateAndTime;
    ArrayList<SubComment> subComments;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ArrayList<SubComment> getSubComments() {
        return subComments;
    }

    public void setSubComments(ArrayList<SubComment> subComments) {
        this.subComments = subComments;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getComments() {
        return comment;
    }

    public void setComments(String comments) {
        this.comment = comments;
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
