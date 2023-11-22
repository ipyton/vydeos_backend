package com.chen.blogbackend.controllers;


import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.Util.AccountInfoValidator;
import com.chen.blogbackend.Util.TokenUtil;
import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Token;
import com.chen.blogbackend.services.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
@RequestMapping("account")
public class AccountController {

    @Autowired
    AccountService accountService;

    @PostMapping("/login")
    public LoginMessage login(@Param("email") String email, @Param("password") String password)
    {
        System.out.println(email);
        System.out.println(password);
        if(accountService.validatePassword(email, password)) {
            Token token = TokenUtil.createToken(new Token(email,null,30 * 3600 * 24));
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

    @PostMapping("/info")
    public Account getAccountInformation(HttpServletRequest request) {
        accountService.selectAccount("sdfsdf");
        String token = request.getHeader("token");
        Token token1 = TokenUtil.resolveToken(token);
        System.out.println(token1.getEmail() + token1.getExpireDatetime());
        System.out.println(token1.getEmail());
        return accountService.selectAccount(token1.getEmail());
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
