package com.chen.blogbackend.controllers;

import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("comment")
public class CommentsController {

    @Autowired
    CommentService commentService;

    @PostMapping("get")
    public LoginMessage getComment(String commentID, int from, int to) {
        return new LoginMessage(-1, "");
    }

    @PostMapping("set")
    public LoginMessage setComment(String comment, String content) {
        return new LoginMessage(-1, "");
    }


}
