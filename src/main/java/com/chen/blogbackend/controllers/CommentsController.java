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
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("comment")
@Controller()
@ResponseBody
public class CommentsController {

    @Autowired
    CommentService commentService;

    @RequestMapping("get_by_object")
    public PagingMessage<Comment> getCommentsByObject(String objectId, String pagingState) {
        commentService.getCommentByObjectId(objectId, pagingState);
        return new PagingMessage<>();
    }

    @RequestMapping("get_by_comment")
    public PagingMessage<Comment> getCommentByCommentId(String commentId, String pagingState) {
        commentService.getCommentByCommentId(commentId, pagingState);
        return new PagingMessage<>();
    }

    @RequestMapping("get_user_comments")
    public PagingMessage<Comment> getUserComments(String userId,String pagingState){
        commentService.getCommentByUserId(userId, pagingState);
        return new PagingMessage<>();
    }


    @RequestMapping("get_app_comment")
    public PagingMessage<ApplicationComment> getAppComment(String applicationId, String pagingState){
        commentService.getApplicationComment(applicationId, pagingState);
        return new PagingMessage<>();
    }

    @RequestMapping("set")
    public LoginMessage setCommentByObject(String objectId, Comment comment) {
        commentService.addComment(objectId, comment, false);
        return new LoginMessage(-1, "");
    }

    @RequestMapping("set_sub_comment")
    public LoginMessage setCommentForComment(String objectId, Comment comment) {
        commentService.addComment(objectId, comment, true);
        return new LoginMessage(-1, "");
    }

    @RequestMapping("set_app")
    public LoginMessage setCommentForApplication(ApplicationComment comment){
        commentService.addApplicationComment(comment);
        return new LoginMessage(-1, "");
    }

    @RequestMapping("like")
    public LoginMessage likeComment(String objectId, String commentId) {
        commentService.like(objectId, commentId);
        return new LoginMessage(-1, "");
    }


    @RequestMapping("dislike")
    public LoginMessage dislikeComment(String objectId, String commentId) {
        commentService.dislike(objectId, commentId);
        return new LoginMessage(-1, "");
    }





}
