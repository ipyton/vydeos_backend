package com.chen.blogbackend.controllers;

import com.chen.blogbackend.entities.ApplicationComment;
import com.chen.blogbackend.entities.Comment;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("comment")
public class CommentsController {

    @Autowired
    CommentService commentService;

    @RequestMapping("get_by_object")
    public PagingMessage<Comment> getCommentsByObject(String objectId) {
        commentService.getCommentByObjectId(objectId);
        return new PagingMessage<>();
    }

    @RequestMapping("get_by_comment")
    public PagingMessage<Comment> getCommentByCommentId(String commentId) {
        commentService.getCommentByCommentId(commentId);
        return new PagingMessage<>();
    }

    @RequestMapping("get_user_comments")
    public PagingMessage<Comment> getUserComments(String userId){
        commentService.getCommentByUserId(userId);
        return new PagingMessage<>();
    }


    @RequestMapping("get_app_comment")
    public PagingMessage<ApplicationComment> getAppComment(String applicationId){
        commentService.getApplicationComment(applicationId);
        return new PagingMessage<>();
    }

    @RequestMapping("set")
    public LoginMessage setCommentByObject(Comment comment) {
        commentService.addCommentForContent(comment);
        return new LoginMessage(-1, "");
    }

    @RequestMapping("set_app")
    public LoginMessage setCommentForApplication(ApplicationComment comment){
        commentService.addApplicationComment(comment);
        return new LoginMessage(-1, "");
    }



}
