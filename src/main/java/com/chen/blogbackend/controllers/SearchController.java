package com.chen.blogbackend.controllers;

import com.chen.blogbackend.responseMessage.LoginMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller("search")
public class SearchController {


    @PostMapping("get")
    public LoginMessage getSearchResult(String keyword) {
        return new LoginMessage(-1, "");
    }

    @PostMapping("set")
    public LoginMessage setSearch() {
        return new LoginMessage(-1, "");
    }

    @PostMapping("suggestion")
    public LoginMessage getSuggestions() {
        return new LoginMessage(-1, "");
    }

}
