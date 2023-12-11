package com.chen.blogbackend.controllers;

import com.chen.blogbackend.ResponseMessage.LoginMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.juli.logging.Log;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.RequestScope;

@Controller("message")
public class MessageController {

    @RequestMapping("get_messages")
    public LoginMessage getMessagesByUserId(HttpServletRequest request){
        return new LoginMessage(-1, "");
    }

    @RequestMapping("send_message")
    public LoginMessage sendMessage(HttpServletRequest request, String to, String message) {
        return new LoginMessage(-1, "");
    }

    @RequestMapping("join")
    public LoginMessage joinGroup(){
        return new LoginMessage(-1, "");
    }

    @RequestMapping("quit")
    public LoginMessage quitGroup() {
        return new LoginMessage(-1, "");
    }

    @RequestMapping("remove")
    public LoginMessage removeUserFromGroup() {
        return new LoginMessage(-1, "");
    }

    @RequestMapping("invite")
    public LoginMessage makeInvitation() {
        return new LoginMessage(-1, "");
    }

    @RequestMapping("dismiss")
    public LoginMessage dismissGroup() {
        return new LoginMessage(-1, "");
    }

    @RequestMapping("join_by_invitation")
    public LoginMessage joinGroupByInvitation() {
        return new LoginMessage(-1, "");
    }

    @RequestMapping("get_members")
    public LoginMessage getMembers() {
        return new LoginMessage(-1, "");
    }

}
