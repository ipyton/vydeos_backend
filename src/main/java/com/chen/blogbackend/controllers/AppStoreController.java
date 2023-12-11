package com.chen.blogbackend.controllers;

import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("app")
public class AppStoreController {

    @Autowired
    ApplicationService service;

    @RequestMapping("getAllApplications")
    public LoginMessage getAllApplications(String index) {

        return new LoginMessage(-1, "");
    }


    @RequestMapping("getInstalledApplications")
    public LoginMessage getInstalledApplications() {
        return new LoginMessage(-1, "");
    }

    @RequestMapping("del")
    public boolean deleteApplication() {
        return false;

    }

    @RequestMapping("upload")
    public LoginMessage uploadApplication() {

        return new LoginMessage(-1, "");
    }

    @RequestMapping("rate")
    public LoginMessage rateApplication() {
        return new LoginMessage(-1, "");
    }





}
