package com.chen.blogbackend.controllers;


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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

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
    public LoginMessage uploadAvatar(MultipartFile multipartFile, HttpServletRequest request){
        boolean result = pictureService.uploadAvatarPicture(request.getHeader("userEmail"), multipartFile);
        if(!result) return new LoginMessage(-1, "hello");
        else return new LoginMessage(1, "success");
    }


    @PostMapping(value = "/uploadArticlePics")
    public LoginMessage uploadArticlePics(MultipartFile multipartFile, String articleID){
        boolean result = pictureService.uploadAvatarPicture(articleID, multipartFile);
        if(!result) return new LoginMessage(-1, "hello");
        else return new LoginMessage(1, "success");
    }



    @PostMapping("/info")
    public Account getAccountInformation(HttpServletRequest request) {
        accountService.selectAccount("sdfsdf");
        String token = request.getHeader("token");
        Token token1 = TokenUtil.resolveToken(token);
        System.out.println(token1.getUserEmail() + token1.getExpireDatetime());
        System.out.println(token1.getUserEmail());
        return accountService.selectAccount(token1.getUserEmail());
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

}
