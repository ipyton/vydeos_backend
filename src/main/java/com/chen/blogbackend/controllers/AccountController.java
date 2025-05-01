package com.chen.blogbackend.controllers;


import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.Message;
import com.chen.blogbackend.util.AccountInfoValidator;
import com.chen.blogbackend.util.TokenUtil;
import com.chen.blogbackend.services.AccountService;
import com.chen.blogbackend.services.PictureService;
import com.chen.blogbackend.util.ValidationResult;
import com.sun.tools.jconsole.JConsoleContext;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Controller
@ResponseBody
@RequestMapping("/account")
public class AccountController {

    @Autowired
    AccountService accountService;

    @Autowired
    PictureService pictureService;

    @PostMapping("/login")
    public LoginMessage login(@Param("email") String email, @Param("password") String password, @Param("remember") Boolean remember) {
        float days = 0.2f;
        if (remember == true) {
            days = 30;
        }
        Auth auth = accountService.validatePassword(email, password);

        if (auth != null) {
            Token token = TokenUtil.createToken(new Token(email, auth.getRoleid(),Instant.now().plus((long) (days * 3600 * 24), ChronoUnit.SECONDS), email));
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
            if (accountService.insertStep1(userId)) return new LoginMessage(1, "Successfully");
        }
        return new LoginMessage(-1, "register error");
    }

    @PostMapping("/sendVerificationCode")
    public Message sendVerification(String userId) {
        try {
            accountService.sendVerificationEmail(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(-1, "failed");
        }
        return new Message(0, "Success!");
    }

    @PostMapping("/registerStep2")
    public LoginMessage registerStep2(Auth accountInfo) {

        return new LoginMessage(1, "register error");
    }

    @PostMapping("/registerStep3")
    public LoginMessage registerStep3(String password, String userId) {
        ValidationResult validationResult = AccountInfoValidator.validateUserPassword(password);
        if (validationResult.getCode() == 0) {
            if (accountService.insertStep3(password, userId)) return new LoginMessage(1, "Registered Successfully");
        }
        return new LoginMessage(-1, validationResult.getResult());
    }

    @PostMapping("/changePassword")
    public LoginMessage changePassword(Account accountInfo) {

        return new LoginMessage(-1, "hello");
    }


    @PostMapping(value = "/uploadAvatar")
    public LoginMessage uploadAvatar(@RequestParam("avatar") MultipartFile multipartFile,HttpServletRequest request) {
        System.out.println(request);
        boolean result = pictureService.uploadAvatarPicture((String) request.getAttribute("userEmail"), multipartFile);
        if (!result) return new LoginMessage(-1, "error");
        else return new LoginMessage(1, "success");
    }

    @GetMapping(value = "/getAvatar")
    public String getAvatar(HttpServletRequest request) throws IOException {
        System.out.println(request.getAttribute("userEmail"));
        InputStream fileStream = pictureService.getAvatar((String) request.getAttribute("userEmail"));
        if (fileStream == null) {
            return "data:image/jpeg;base64," + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/wcAAwAB/08FHiYAAAAASUVORK5CYII=";
        }
        byte[] bytes = fileStream.readAllBytes();
        String encodedString = java.util.Base64.getEncoder().encodeToString(bytes);

        // 返回 Base64 编码的图片数据 URI，用于前端渲染
        return "data:image/jpeg;base64," + encodedString;
    }

    //get a user's info.
    @PostMapping("/getinfo")
    public LoginMessage getAccountInformation(HttpServletRequest request) {
        String userEmail = (String) request.getAttribute("userEmail");

        Account result = accountService.getAccountDetailsById(userEmail);
        if (null == result) {
            return new LoginMessage(-1, "Do not have get any information.");
        }
        return new LoginMessage(1, JSON.toJSONString(result));
    }

    @GetMapping("/getAuthById")
    public LoginMessage getInfoById(@Param("userEmail") String userEmail) {
        System.out.println(userEmail);
        Auth result = accountService.getAccountRoleById(userEmail);
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
    public LoginMessage verifyToken(@RequestBody Map<String, String> requestBody) {
        String token = requestBody.get("token");

        System.out.println("------=======-----" +token);
        if (null != token) {
            if (accountService.haveValidLogin(token)) {
                System.out.println("---------------------return");
                return new LoginMessage(1, TokenUtil.resolveToken(token).getUserId());
            }
        }
        return new LoginMessage(-1, "invalid token");
    }

    @PostMapping("/reset_password")
    public LoginMessage resetPassword(HttpServletRequest request,@Param("oldPassword") String oldPassword, @Param("newPassword") String newPassword ) {
        Object email = request.getAttribute("userEmail");
        boolean result = accountService.resetPassword(email, oldPassword, newPassword);
        if (result) {
            return new LoginMessage(1, "Success");
        }
        else {
            return new LoginMessage(-1, "reset password error");
        }
    }



    @PostMapping("/deleteUser")
    public LoginMessage deleteAccount(String userId) {
        boolean result = accountService.deleteUser(userId);
        if (result) {
            return new LoginMessage(1, "Success");
        } else {
            return new LoginMessage(-1, "change role error");
        }
    }

    @GetMapping("/auth")
    public ResponseEntity<String> auth(HttpServletRequest request) {
        String token = request.getHeader("Token");
        ResponseEntity<String> response; // Declare a variable to hold the response
        try {
            if (accountService.haveValidLogin(token)) {
                response = new ResponseEntity<>("Success message", HttpStatus.OK);  // 200 OK
            } else {
                response = new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = new ResponseEntity<>("Error occurred", HttpStatus.INTERNAL_SERVER_ERROR); // You can customize this error response
        }
        return response; // Return the response after the try-catch-finally block
    }



}
