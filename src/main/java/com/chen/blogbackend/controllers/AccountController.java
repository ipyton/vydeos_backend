package com.chen.blogbackend.controllers;


import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.Auth;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.Message;
import com.chen.blogbackend.util.AccountInfoValidator;
import com.chen.blogbackend.util.TokenUtil;
import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Token;
import com.chen.blogbackend.services.AccountService;
import com.chen.blogbackend.services.PictureService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;

@Controller
@ResponseBody
@RequestMapping("/account")
public class AccountController {

    @Autowired
    AccountService accountService;

    @Autowired
    PictureService pictureService;

    @PostMapping("/login")
    public LoginMessage login(@Param("email") String email, @Param("password") String password) {

        if (accountService.validatePassword(email, password)) {
            Token token = TokenUtil.createToken(new Token(email, Instant.now().plus(30 * 3600 * 24, ChronoUnit.SECONDS), null));
            System.out.println("account service");
            if (accountService.setToken(token)) {
                return new LoginMessage(1, token.getTokenString());
            }
        }
        return new LoginMessage(-1, "Check your user name and password!");
    }

    @PostMapping("/registerStep1")
    public LoginMessage registerStep1(String userId) {
        System.out.println(userId);
        if (AccountInfoValidator.validateUserEmail(userId)) {
            if (accountService.insertStep1(userId)) return new LoginMessage(1, "Registered Successfully");
        }
        return new LoginMessage(-1, "register error");
    }

    @PostMapping("/registerStep2")
    public LoginMessage registerStep2(Auth accountInfo) {
        return new LoginMessage(1, "register error");
    }

    @PostMapping("/registerStep3")
    public LoginMessage registerStep3(String password, String userId) {
        if (AccountInfoValidator.validateUserPassword(password)) {
            if (accountService.insertStep3(password, userId)) return new LoginMessage(1, "Registered Successfully");
        }
        return new LoginMessage(-1, "register error");
    }

    @PostMapping("/changePassword")
    public LoginMessage changePassword(Account accountInfo) {

        return new LoginMessage(-1, "hello");
    }


    @PostMapping(value = "/uploadAvatar")
    public LoginMessage uploadAvatar(@RequestParam("avatar") MultipartFile multipartFile,HttpServletRequest request) {
        System.out.println(request);
        boolean result = pictureService.uploadAvatarPicture(request.getHeader("userEmail"), multipartFile);
        if (!result) return new LoginMessage(-1, "hello");
        else return new LoginMessage(1, "success");
    }


    //get a user's info.
    @PostMapping("/getinfo")
    public LoginMessage getAccountInformation(HttpServletRequest request) {
        String userEmail = request.getHeader("userEmail");
        Account result = accountService.selectAccount(userEmail);
        if (null == result) {
            return new LoginMessage(-1, "Do not have get any information.");
        }
        return new LoginMessage(1, JSON.toJSONString(result));
    }

    @PostMapping("/setinfo")
    public LoginMessage setAccountInformation(Account account)  {
        boolean result = accountService.insertUserDetails(account);
        if (result) {
            return new LoginMessage(1, "Success");
        }
        return new LoginMessage(-1, "setError");
    }

    @PostMapping("/verifyToken")
    public LoginMessage verifyToken(@Param("token") String token) {
        System.out.println(token);
        if (null != token) {
            if (accountService.haveValidLogin(token)) {
                return new LoginMessage(1, TokenUtil.resolveToken(token).getUserId());
            }
        }
        return new LoginMessage(-1, "invalid token");
    }


    @PostMapping("/getAvatar")
    public ResponseEntity<StreamingResponseBody> getAvatar(HttpServletRequest request) {
        String userEmail = request.getHeader("userEmail");
        if (null == userEmail) {
            return (ResponseEntity<StreamingResponseBody>) ResponseEntity.notFound();
        }
        System.out.println(userEmail);
        StreamingResponseBody avatar = pictureService.getAvatar(userEmail);

        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(avatar);
    }

}
