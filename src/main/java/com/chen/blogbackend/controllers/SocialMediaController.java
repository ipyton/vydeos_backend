package com.chen.blogbackend.controllers;

import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.services.CommentService;
import com.chen.blogbackend.services.SocialMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("friends")
public class SocialMediaController {

    @Autowired
    SocialMediaService socialMediaService;


    @PostMapping("follow")
    public LoginMessage follow(String userEmail, String userToFollow, String groupToAdd) {
        return new LoginMessage(-1, "");
    }

    @PostMapping("unfollow")
    public LoginMessage unfollow(String userEmail, String userFollowed) {
        return new LoginMessage(-1, "");
    }

    @PostMapping("setGroup")
    public LoginMessage setUserGroup(){
        return new LoginMessage(-1, "message");
    }

    @PostMapping("getGroup")
    public LoginMessage getUserGroup(){
        return new LoginMessage(-1, "message");
    }

    @PostMapping("getGroupMembers")
    public LoginMessage getGroupMembers(){
        return  new LoginMessage(-1, "");
    }

    @RequestMapping("")
    public LoginMessage getFollowers(){
        return new LoginMessage(-1, "");
    }

    @RequestMapping("")
    public LoginMessage getAttentions(){
        return new LoginMessage(-1, "");
    }

    @RequestMapping("")
    public LoginMessage changeGroup() {
        return new LoginMessage(-1, "");
    }

    @RequestMapping("")
    public LoginMessage addGroup() {
        return new LoginMessage(-1, "");
    }

    @RequestMapping("")
    public LoginMessage deleteGroup() {
        return new LoginMessage(-1, "");
    }

}

