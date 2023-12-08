package com.chen.blogbackend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("app")
public class AppStoreController {

    @RequestMapping("get")
    public boolean getApplication() {
        return true;
    }

    @RequestMapping("")
    public boolean deleteApplication() {
        return false;

    }
}
