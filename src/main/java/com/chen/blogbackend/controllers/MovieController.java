package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.MovieDownloadRequest;
import com.chen.blogbackend.entities.Playable;
import com.chen.blogbackend.entities.SeasonMeta;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.Message;
import com.chen.blogbackend.services.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("isStared")
    public Message isStared(HttpServletRequest request, String resourceId, String type) {
        String email = (String) request.getAttribute("userEmail");
        boolean result = videoService.isStared(email, resourceId, type);
        if (result) return new Message(0, "stared");
        else return new Message(0, "not");
    }

    @RequestMapping("/sendRequest")
    public Message sendRequest(HttpServletRequest request, String resourceId, String type, String language){
        String email = (String) request.getAttribute("userEmail");
        boolean b = videoService.sendRequest(email, resourceId, type, language);
        if (b) return new Message(200, "success");
        else return new Message(201, "fail");
    }

    @RequestMapping("/getRequests")
    public Message getRequests(){
    try {
        List<MovieDownloadRequest> requests = videoService.getRequests();
        return new Message(0, JSON.toJSONString(requests));
    } catch (Exception e) {
        e.printStackTrace();
        return new Message(-1, "fail");
    }

    }

    @RequestMapping("/isRequested")
    public boolean isRequested(String videoId,String type){
        return videoService.isRequested(videoId,type);
    }

    @RequestMapping("/getPlayable")
    public Message getPlayable(String resourceId, String type, Integer season ){
        if (resourceId == null || resourceId.isEmpty() || type == null || type.isEmpty()) return new Message(-1, "fail");
        List<Playable> playableList = videoService.getPlayable(resourceId, type, season);
        return new Message(0, JSON.toJSONString(playableList));
    }

    @RequestMapping("/isPlayable")
    public Message isPlayable(String resourceId, String type ){
        try {
            return new Message(0,JSON.toJSONString(videoService.isPlayable(resourceId, type)));

        } catch (Exception e) {
            return new Message(-1, e.getMessage());
        }
    }

    @RequestMapping("/get_season_meta")
    public Message getSeasonMeta(String resourceId, String type, Integer seasonId){
        SeasonMeta meta = videoService.getSeasonMeta(resourceId, type, seasonId);
        return new Message(0, JSON.toJSONString(meta));
    }

}
