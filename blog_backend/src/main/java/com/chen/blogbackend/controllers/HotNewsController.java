package com.chen.blogbackend.controllers;

import com.chen.blogbackend.responseMessage.LoginMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("hot")
@Controller()
@ResponseBody
public class HotNewsController {

    @RequestMapping("posts/get")
    public LoginMessage getHotPosts() {

        return new LoginMessage(-1, "");
    }


    @RequestMapping("set")
    public LoginMessage setHotPosts() {

        return new LoginMessage(-1, "");
    }

}
