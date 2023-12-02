package com.chen.blogbackend.controllers;


import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.Util.AccountInfoValidator;
import com.chen.blogbackend.Util.TokenUtil;
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

import java.io.InputStream;
import java.util.Calendar;

@Controller
@ResponseBody
@RequestMapping("account")
public class AccountController {

    @Autowired
    AccountService accountService;

    @Autowired
    PictureService pictureService;

    @PostMapping("/login")
    public LoginMessage login(@Param("email") String email, @Param("password") String password)
    {
        System.out.println(email);
        System.out.println(password);
        if(accountService.validatePassword(email, password)) {
            Calendar instance = Calendar.getInstance();
            instance.add(Calendar.SECOND, 30 * 3600 * 24);
            Token token = TokenUtil.createToken(new Token(email,instance.getTime(),null));
            if(0 != accountService.setToken(token)) {
                return new LoginMessage(1, token.getTokenString());
            }
        }
        return new LoginMessage(-1, "Check your user name and password!");
    }

    @PostMapping("/register")
    public LoginMessage register(Account accountInfo) {
        System.out.println(accountInfo);
        if (AccountInfoValidator.validateAccount(accountInfo)) {
            System.out.println("sdgfisiodf");
            if (accountService.insert(accountInfo)) return new LoginMessage(1, "Registered Successfully");
        }
        return new LoginMessage(-1, "register error");
    }

    @PostMapping("/changePassword")
    public LoginMessage changePassword(Account accountInfo) {

        return new LoginMessage(-1, "hello");
    }


    @PostMapping(value = "/uploadAvatar")
    public LoginMessage uploadAvatar(@RequestParam("avatar") MultipartFile multipartFile, HttpServletRequest request){
        System.out.println(request);
        boolean result = pictureService.uploadAvatarPicture(request.getHeader("userEmail"), multipartFile);
        if(!result) return new LoginMessage(-1, "hello");
        else return new LoginMessage(1, "success");
    }


    @PostMapping("/getinfo")
    public LoginMessage getAccountInformation(HttpServletRequest request) {
        String userEmail = request.getHeader("userEmail");
        Account result = accountService.selectAccount(userEmail);
        if (null == result) {
            return new LoginMessage(-1, "error");
        }
        return new LoginMessage(1, JSON.toJSONString(result));
    }

    @PostMapping("/setinfo")
    public LoginMessage setAccountInformation(Account account) {
        boolean result = accountService.update(account);
        if (result) {
            return new LoginMessage(1, "Success");
        }
        return new LoginMessage(-1, "setError");
    }


    @PostMapping("/verifyToken")
    public LoginMessage verifyToken(@Param("token") String token) {
        System.out.println(token);
        if (null != token) {
            if(accountService.haveValidLogin(token)){
                return new LoginMessage(1,"valid token!");
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
