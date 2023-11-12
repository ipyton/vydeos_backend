package com.chen.blogbackend.controllers;


import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.Util.AccountInfoValidator;
import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.services.AccountService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("account")
public class AccountController {



    @RequestMapping("login")
    public LoginMessage login(@Param("userName") String userName, @Param("password") String password)
    {
        if (AccountService.validate(userName, password)) {
            return new LoginMessage(-1, "Please check your password and username");
        }
        else {
            String token = userName + password;
            return new LoginMessage(1, token);
        }
    }

    @RequestMapping("register")
    public LoginMessage register(Account accountInfo) {
        if (AccountInfoValidator.validateAccount(accountInfo)) {


        }
    }

    @RequestMapping("info")
    public Account getAccountInformation() {

    }


}
