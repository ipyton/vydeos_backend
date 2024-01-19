package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.Message;
import com.chen.blogbackend.services.AuthorityService;
import com.chen.blogbackend.services.FriendsService;
import com.chen.blogbackend.services.SearchService;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.List;

@Controller("search")
public class SearchController {

    @Autowired
    SearchService searchService;

    @Autowired
    AuthorityService authorityService;


    @PostMapping("getChat")
    public LoginMessage getChatSearchResult(String keyword, int from) throws IOException {
        List<SingleMessage> singleMessages = searchService.searchSingleMessage(keyword, from);
        List<GroupMessage> groupMessages = searchService.searchGroupMessage(keyword, from);
        String content = JSON.toJSONString(singleMessages) + "__||||__" + JSON.toJSONString(groupMessages);
        return new LoginMessage(-1, content);
    }

    @PostMapping("setSingleChat")
    public LoginMessage setSingleChat(SingleMessage message) throws IOException, InterruptedException {
        searchService.setSearchSingleMessage(message);
        return new LoginMessage(-1, "");
    }

    @PostMapping("setGroupChat")
    public LoginMessage setGroupChat(GroupMessage message) throws IOException, InterruptedException {
        searchService.setGroupMessage(message);
        return new LoginMessage(-1, "");
    }


    @PostMapping("setUser")
    public LoginMessage setUser(Friend friend) throws IOException, InterruptedException {
        searchService.setUserIndex(friend);
        return new LoginMessage(-1, "");
    }

    @PostMapping("getUser")
    public LoginMessage getUser(String keyword, int from) throws IOException {
        searchService.searchByUser(keyword, from);
         return new LoginMessage(-1, "");
    }

    @PostMapping("setContent")
    public LoginMessage setContent(Article article) throws IOException, InterruptedException {
        searchService.setArticleIndex(article);
        return new LoginMessage(-1, "");
    }

    @PostMapping("getContent")
    public LoginMessage getContent(String userId, String keyword, int from) throws IOException {
        searchService.searchByArticle(userId, keyword, from);
        return new LoginMessage(-1, "");
    }


}
