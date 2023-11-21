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
    public LoginMessage login(@Param("userName") String userName, @Param("password") String password)
    {
        String userId = accountService.validatePassword(userName, password);
        if(userId != null) {
            String token = TokenUtil.createToken(new Token(userId, userName,30 * 3600 * 24));
            Token token1 = TokenUtil.resolveToken(token);
            System.out.println(token1.getUserID());
            System.out.println(token1.getUsername());
            System.out.println(token1.getExpiresDateAndTime());

            return new LoginMessage(1, token);
        }
        else return new LoginMessage(-1, "Check your user name and password!");
    }

    @PostMapping("/register")
    public LoginMessage register(Account accountInfo) {
        if (AccountInfoValidator.validateAccount(accountInfo)) {
            accountService.insert(accountInfo);
            return new LoginMessage(1, "Regist Successfully");
        }
        else return new LoginMessage(-1, "register error");

    }

    @PostMapping("/info")
    public Account getAccountInformation(HttpServletRequest request) {
        String token = request.getHeader("token");
        Token token1 = TokenUtil.resolveToken(token);
        System.out.println(token1.getUserID() + token1.getUsername() + token1.getExpiresDateAndTime());
        if (null == token1) {
            return new Account();
        }
        System.out.println(token1.getUserID());
        return accountService.selectAccount(token1.getUserID());
    }


}
