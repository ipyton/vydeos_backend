package com.chen.blogbackend.controllers;

import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.services.CommentService;
import com.chen.blogbackend.services.SocialMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SocialMediaController {

    @Autowired
    SocialMediaService socialMediaService;


    @PostMapping("friends/follow")
    public LoginMessage follow(String userEmail, String userToFollow, String groupToAdd) {
        return new LoginMessage(-1, "");
    }

    @PostMapping("friends/unfollow")
    public LoginMessage unfollow(String userEmail, String userFollowed) {
        return new LoginMessage(-1, "");
    }

    @PostMapping("/friends/setGroup")
    public LoginMessage setUserGroup(){
        return new LoginMessage(-1, "message");
    }

    @PostMapping("/friends/getGroup")
    public LoginMessage getUserGroup(){
        return new LoginMessage(-1, "message");
    }

    @PostMapping("/friends/getGroupMembers")
    public LoginMessage getMembers(){
        return  new LoginMessage(-1, "");
    }
}

