package com.chen.blogbackend.controllers;

import com.chen.blogbackend.entities.App;
import com.chen.blogbackend.entities.ApplicationComment;
import com.chen.blogbackend.entities.Comment;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.services.AccountService;
import com.chen.blogbackend.services.ApplicationService;
import com.chen.blogbackend.util.PagingStateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller()
@RequestMapping("app")
@ResponseBody
public class AppStoreController {

    @Autowired
    ApplicationService service;


    @RequestMapping("getAllApplications")
    public PagingMessage<App> getAllApplications(String index) {
        return service.getPagingIntroductions(PagingStateConverter.stringToConverter(index));
    }


    @RequestMapping("getInstalledApplications")
    public PagingMessage<App> getInstalledApplications(String userId) {
        return service.getInstalledApps(userId);
    }

    @RequestMapping("del")
    public boolean deleteApplication(String userId, String appId) {
        return service.deleteApplication(userId, appId);
    }

    @RequestMapping("upload")
    public LoginMessage uploadApplication(App app) {
        service.uploadApplication(app);
        return new LoginMessage(-1, "");
    }

    @RequestMapping("rate")
    public LoginMessage rateApplication(ApplicationComment comment) {
        service.comment(comment);
        return new LoginMessage(-1, "");
    }





}
