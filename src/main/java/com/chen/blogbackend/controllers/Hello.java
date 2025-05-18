package com.chen.blogbackend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("hello")
@Controller()
@ResponseBody
public class Hello {

    @RequestMapping("hello")
    public String hello() {
        return "hello";
    }


}
