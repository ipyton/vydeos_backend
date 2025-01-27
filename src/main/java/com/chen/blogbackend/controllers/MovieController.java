package com.chen.blogbackend.controllers;

import com.chen.blogbackend.entities.MovieDownloadRequest;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.services.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("movie")
@ResponseBody()
public class MovieController {

    @Autowired
    private VideoService videoService;

    @PostMapping("uploadMeta")
    public LoginMessage uploadMovieMetadata() {
        // return a endpoint to upload the files
        return new LoginMessage(1, "success");
    }

    @PostMapping("getMeta")
    public LoginMessage getMovieMetadata(String movieName) {

        return new LoginMessage(-1, "success");

    }

    @PostMapping("uploadMovie")
    public LoginMessage getMovieMessages() {
        return new LoginMessage(-1, "success");
    }

    @RequestMapping("/sendRequest")
    public LoginMessage sendRequest(HttpServletRequest request, String videoId){
        String email = (String) request.getAttribute("userEmail");
        boolean b = videoService.sendRequest(email, videoId);
        if (b) return new LoginMessage(200, "success");
        else return new LoginMessage(201, "fail");
    }

    @RequestMapping("/getRequests")
    public List<MovieDownloadRequest> getRequests(){
        return videoService.getRequests();
    }

    @RequestMapping("/isRequested")
    public boolean isRequested(String videoId){
        return videoService.isRequested(videoId);
    }

}
