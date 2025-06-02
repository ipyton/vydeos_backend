package com.chen.blogbackend.controllers;

import com.chen.blogbackend.entities.Settings;
import com.chen.blogbackend.responseMessage.LoginMessage;

import com.chen.blogbackend.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

@RequestMapping("settings")
@Controller()
@ResponseBody
public class SettingsController {

    @Autowired
    SettingsService service;

    @PostMapping("get")
    public LoginMessage getSettings(String userEmail) {
        System.out.println("");
        return new LoginMessage(-1, "");
    }

    @PostMapping("set")
    public LoginMessage setSettings(ArrayList<Settings> setting){
        System.out.println("");
        return new LoginMessage(-1, "setttings");
    }


}
