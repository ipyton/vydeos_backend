package com.chen.blogbackend.controllers;

import com.chen.blogbackend.responseMessage.LoginMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("hot")
@Controller()
@ResponseBody
public class HotNewsController {

    @RequestMapping("get")
    public LoginMessage getHotNews() {

        return new LoginMessage(-1, "");
    }


    @RequestMapping("set")
    public LoginMessage setHotNews() {

        return new LoginMessage(-1, "");
    }

}
