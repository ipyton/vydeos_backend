package com.chen.blogbackend.controllers;

import com.chen.blogbackend.entities.Article;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.Message;
import com.chen.blogbackend.services.AuthorityService;
import com.chen.blogbackend.services.FriendsService;
import com.chen.blogbackend.services.SearchService;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller("search")
public class SearchController {

    @Autowired
    SearchService searchService;

    @Autowired
    AuthorityService authorityService;


    @PostMapping("getChat")
    public LoginMessage getChatSearchResult(String keyword) {
        return new LoginMessage(-1, "");
    }

    @PostMapping("setChat")
    public LoginMessage setChat(Message message) {
        return new LoginMessage(-1, "");
    }


    @PostMapping("setUser")
    public LoginMessage setUser(Friend friend) {
        return new LoginMessage(-1, "");
    }

    @PostMapping("getUser")
    public LoginMessage getUser(String keyword) {
         return new LoginMessage(-1, "");
    }

    @PostMapping("setContent")
    public LoginMessage setContent(Article article){
        return new LoginMessage(-1, "");
    }

    @PostMapping("getContent")
    public LoginMessage getContent(String keyword) {
        return new LoginMessage(-1, "");
    }


}
