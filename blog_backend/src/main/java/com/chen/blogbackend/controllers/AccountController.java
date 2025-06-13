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
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
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
import java.util.Collections;
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
        if (email == null || email.isEmpty() || password == null || password.isEmpty() || remember == null) {
            return new LoginMessage(-1, "Email or password is empty");
        }

        float days = 0.2f;
        if (remember == true) {
            days = 30;
        }
        Auth auth = accountService.validatePassword(email, password);

        if (auth != null) {
            Token token = TokenUtil.createToken(new Token(email, auth.getRoleid(),Instant.now().plus((long) (days * 3600 * 24), ChronoUnit.SECONDS), email));
            System.out.println("account service");
            if (accountService.setToken(token)) {
                return new LoginMessage(0, token.getTokenString());
            }
        }
        return new LoginMessage(-1, "Check your user name and password!");
    }

    @PostMapping("/registerStep1")
    public LoginMessage registerStep1(String userId) {

        if (AccountInfoValidator.validateUserEmail(userId)) {
            userId = userId.toLowerCase();
            if (accountService.insertStep1(userId)) return new LoginMessage(0, "Successfully");
        }
        return new LoginMessage(-1, "register error");
    }
    @PostMapping("/resetStep1")
    public LoginMessage resetStep1(String userId) {
        if (AccountInfoValidator.validateUserEmail(userId)) {
            if (accountService.resetStep1(userId)) return new LoginMessage(0, "Successfully");
        }
        return new LoginMessage(-1, "register error");
    }

    @PostMapping("/sendVerificationCode")
    public Message sendVerification(String userId) {
        try {
            if (! accountService.sendVerificationEmail(userId)){
                return new Message(-1, "Send verification failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(-1, "failed");
        }
        return new Message(0, "Success!");
    }

    @PostMapping("/registerStep2")
    public Message registerStep2(String userId, String code) {
        try {
            if (userId == null || userId.isEmpty() || code == null || code.isEmpty()) {
                return new Message(-1, "userId or code is empty");
            }
            userId = userId.toLowerCase();
            String result = accountService.verifyCode(userId, code);
            if ( result != null ) {
                return new Message(0, result);
            }
            else {
                return new Message(-1, "error code");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(-1, "failed");
        }
    }

    @PostMapping("/registerStep3")
    public LoginMessage registerStep3(String token, String password, String userId) {
        if (token == null || password == null || userId == null ||
                token.equals("") || password.equals("") || password.equals(""))
            return new LoginMessage(-1, "check your input!");
        userId = userId.toLowerCase();
        if (!AccountInfoValidator.validateUserEmail(userId)) {
            return new LoginMessage(-1, "register error");
        }
        ValidationResult validationResult = AccountInfoValidator.validateUserPassword(password);
        if (validationResult.getCode() == 0) {
            if (accountService.insertStep3(token, password, userId)) return new LoginMessage(0, "Registered Successfully");
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
        else return new LoginMessage(0, "success");
    }

    @GetMapping(value = "/getAvatar")
    public String getAvatar(HttpServletRequest request) throws IOException {
        String userEmail = (String) request.getAttribute("userEmail");
        userEmail = userEmail.toLowerCase();
        InputStream fileStream = pictureService.getAvatar(userEmail);
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
        userEmail = userEmail.toLowerCase();
        Account result = accountService.getAccountDetailsById(userEmail);
        if (null == result) {
            return new LoginMessage(-1, "Do not have get any information.");
        }
        return new LoginMessage(0, JSON.toJSONString(result));
    }

    @GetMapping("/getAuthById")
    public LoginMessage getInfoById(@Param("userEmail") String userEmail) {
        System.out.println(userEmail);
        userEmail = userEmail.toLowerCase();
        Auth result = accountService.getAccountRoleById(userEmail);
        if (null == result) {
            return new LoginMessage(-1, "Do not have get any information.");
        }
        return new LoginMessage(0, JSON.toJSONString(result));
    }

    @PostMapping("/setinfo")
    public LoginMessage setAccountInformation(Account account)  {
        account.setUserEmail(account.getUserEmail().toLowerCase());
        boolean result = accountService.insertUserDetails(account);
        if (result) {
            return new LoginMessage(0, "Success");
        }
        return new LoginMessage(-1, "setError");
    }


    @PostMapping("/verifyToken")
    public LoginMessage verifyToken(@RequestBody Map<String, String> requestBody) {
        String token = requestBody.get("token");

        if (null != token) {
            if (accountService.haveValidLogin(token)) {
                return new LoginMessage(0, TokenUtil.resolveToken(token).getUserId());
            }
        }
        return new LoginMessage(-1, "invalid token");
    }




    @PostMapping("/deleteUser")
    public LoginMessage deleteAccount(String userId) {
        userId = userId.toLowerCase();
        boolean result = accountService.deleteUser(userId);
        if (result) {
            return new LoginMessage(0, "Success");
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


    @PostMapping("/google")
    public Message verifyGoogleToken(@RequestBody Map<String, String> request) {
        try {
            String tokenString = request.get("idToken");
            if (tokenString == null || tokenString.isEmpty()) {
                return new Message(-1, "No token provided");
            }

            Account account = accountService.signInWithGoogle(tokenString);
            return new Message(0, JSON.toJSONString(account));

        } catch (Exception e) {
            e.printStackTrace();
            return new Message(-1, "Error occurred");
        }
    }



}
