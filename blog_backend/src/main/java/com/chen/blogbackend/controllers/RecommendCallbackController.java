package com.chen.blogbackend.controllers;

import com.chen.blogbackend.responseMessage.LoginMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("recommend")
@ResponseBody
public class RecommendCallbackController {

    @PostMapping("get_articles")
    public LoginMessage getRecommendArticles(String userID, int from, int to) {
        return new LoginMessage(-1, "error");
    }

    @PostMapping("get_users")
    public LoginMessage getRecommendUsers(String userID, int from, int to) {
        return new LoginMessage(-1, "error");
    }

    @PostMapping("get_videos")
    public LoginMessage getRecommendVideos(String userID, int from, int to) {
        return new LoginMessage(-1, "error");
    }



}
