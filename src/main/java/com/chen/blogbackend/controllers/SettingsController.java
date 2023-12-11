package com.chen.blogbackend.controllers;

import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.entities.Setting;

import com.chen.blogbackend.services.SettingsService;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;

@Controller("settings")
public class SettingsController {

    @Autowired
    SettingsService service;

    @PostMapping("get")
    public LoginMessage getSettings(String userEmail) {
        System.out.println("");
        return new LoginMessage(-1, "");
    }

    @PostMapping("set")
    public LoginMessage setSettings(ArrayList<Setting> setting){
        System.out.println("");
        return new LoginMessage(-1, "setttings");
    }


}
