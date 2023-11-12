package com.chen.blogbackend.controllers;


import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.Util.AccountInfoValidator;
import com.chen.blogbackend.Util.PasswordEncryption;
import com.chen.blogbackend.Util.TokenUtil;
import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Token;
import com.chen.blogbackend.services.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.annotations.Param;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("account")
public class AccountController {

    @Autowired
    AccountService accountService;

    @RequestMapping("login")
    public LoginMessage login(@Param("userName") String userName, @Param("password") String password)
    {
        String userId = accountService.validatePassword(userName, password);
        if(userId != null) {
            String token = TokenUtil.createToken(new Token(userId, userName,30 * 3600 * 24));
            return new LoginMessage(1, token);
        }
        else return new LoginMessage(-1, "Check your user name and password!");
    }

    @RequestMapping("register")
    public LoginMessage register(Account accountInfo) {
        if (AccountInfoValidator.validateAccount(accountInfo)) {
            accountService.insert(accountInfo);
            return new LoginMessage(1, "Regist Successfully");
        }
        else return new LoginMessage(-1, "register error");

    }

    @RequestMapping("info")
    public Account getAccountInformation(HttpServletRequest request) {
        String token = request.getHeader("token");
        Token token1 = TokenUtil.resolveToken(token);
        if (null == token1) {
            return new Account();
        }
        return accountService.selectAccount(token1.getUserID());
    }


}
