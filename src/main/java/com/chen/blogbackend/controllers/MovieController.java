package com.chen.blogbackend.controllers;

import com.chen.blogbackend.responseMessage.LoginMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("movies")
@ResponseBody()
public class MovieController {

    @PostMapping("uploadMeta")
    public LoginMessage uploadMovieMetadata() {
        // return a endpoint to upload the files
        return new LoginMessage(1, "success");
    }

    @PostMapping("getMeta")
    public LoginMessage getMovieMetadata(String movieName) {

        return new LoginMessage(-1, "success");

    }
}
