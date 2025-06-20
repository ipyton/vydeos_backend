package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.ApplicationComment;
import com.chen.blogbackend.entities.Comment;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.Message;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.services.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequestMapping("comment")
@Controller()
@ResponseBody
public class CommentsController {

    @Autowired
    CommentService commentService;

    @RequestMapping("get")
    public Message getCommentsByResourceId(String resourceId, String type, String pagingState) {
        try {
            List<Comment> commentByObjectId = commentService.getCommentByResourceId(resourceId, type, pagingState);
            return new Message(0, JSON.toJSONString(commentByObjectId));
        }
        catch (Exception e) {
            return new Message(-1, e.getMessage());
        }
    }

    @RequestMapping("send")
    public Message sendComment(HttpServletRequest request,String  content, String resourceId, String type, String pagingState) {
        try {
            String userEmail = (String) request.getAttribute("userEmail");
            boolean result = commentService.sendComment(userEmail, content,resourceId,type,0l);
            if (result) {
                return new Message(0, "success");
            }
            else {
                return new Message(-1, "fail");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return new Message(-1, e.getMessage());
        }
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
