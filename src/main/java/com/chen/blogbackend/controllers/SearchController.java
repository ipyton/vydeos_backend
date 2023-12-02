package com.chen.blogbackend.controllers;

import com.chen.blogbackend.ResponseMessage.LoginMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller("search")
public class SearchController {


    @PostMapping("set")
    public LoginMessage getSearchResult(String keyword) {
        return new LoginMessage(-1, "");
    }

    @PostMapping("search")
    public LoginMessage setSearch() {
        return new LoginMessage(-1, "");
    }


}
