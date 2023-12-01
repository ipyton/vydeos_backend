package com.chen.blogbackend.controllers;

import com.chen.blogbackend.ResponseMessage.LoginMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
