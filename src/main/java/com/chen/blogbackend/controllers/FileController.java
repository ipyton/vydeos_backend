package com.chen.blogbackend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("file")
public class FileController {
    // manage all files movie.cql and upload.

    @PostMapping("getAvatar")
    public String getAvatar() {
        return "";
    }

    @PostMapping("setAvatar")
    public String setAvatar() {
        return "";
    }

    @PostMapping("uploadPics")
    public String uploadPics(String name) {
        return "";
    }

    @PostMapping("downloadPics")
    public String downloadPics(String name) {
        return "";
    }

    @PostMapping("uploadVoice")
    public String uploadVoice() {
        return "";
    }

    @PostMapping("downloadVoice")
    public String downloadVoice() {
        return "";
    }

    @PostMapping("uploadVideo")
    public String uploadVideo() {
        return "";
    }

    @PostMapping("downloadVideo")
    public String downloadVideo() {
        return "";
    }


}
