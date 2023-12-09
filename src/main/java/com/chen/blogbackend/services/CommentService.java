package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Comment.Comment;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    @Autowired
    CqlSession session;

    PreparedStatement getCommentsByObjectId;
    PreparedStatement setCommentByCommentId;
    PreparedStatement getCommentsByUserId;
    PreparedStatement setCommentByObjectId;

    @PostConstruct
    public void init(){
        getCommentsByObjectId = session.prepare("");
        setCommentByCommentId = session.prepare("");
        getCommentsByUserId = session.prepare("");
        setCommentByObjectId = session.prepare("");
    }

    public Comment getCommentByObjectId(String objectId) {
        return new Comment();
    }

    public Comment getCommentByUserId(String userId) {
        return new Comment();
    }

    public boolean addCommentByObjectId(String objectName) {
        return true;
    }

    public boolean addCommentByCommentId(String comment,String content) {
        return false;
    }


    public boolean deleteComment(String commentID) {
        return true;
    }

    public boolean like(String commentID) {
        return true;
    }

}
