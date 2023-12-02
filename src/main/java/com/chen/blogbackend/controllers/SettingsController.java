package com.chen.blogbackend.controllers;

import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.entities.Setting;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;

@Controller("settings")
public class SettingsController {

    @PostMapping("get")
    public LoginMessage getSettings(String userEmail) {
        return new LoginMessage(-1, "");
    }

    @PostMapping("set")
    public LoginMessage setSettings(ArrayList<Setting> setting){
        return new LoginMessage(-1, "setttings");
    }


}
