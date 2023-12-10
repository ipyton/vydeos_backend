package com.chen.blogbackend.controllers;

import com.chen.blogbackend.ResponseMessage.LoginMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.RequestScope;

@Controller("/controller")
public class UserRelationshipController {

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

    @RequestMapping("")
    public LoginMessage follow() {
        return new LoginMessage(-1, "");
    }
}
