package com.chen.blogbackend.controllers;


import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.services.AccountService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AccountController {

    @Autowired
    AccountService service;


    @RequestMapping("login")
    public LoginMessage login()
    {
        if (service.validate(userName, password)) {
            return new LoginMessage(-1, "Please check your password and username");
        }
        else {
            String token = userName + password;
            return new LoginMessage(1, token);
        }
    }

    @RequestMapping("register")
    public LoginMessage register(@Param("userName") String userName, @Param("password") String password) {
        if (service.validate(userName, password)) {
            return new LoginMessage(-1, "Please check your password and username");
        }
        else {
            String token = userName + password;
            return new LoginMessage(1, token);
        }
    }

}
