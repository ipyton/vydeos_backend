package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.MovieDownloadRequest;
import com.chen.blogbackend.entities.Video;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.services.VideoService;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import java.util.List;

@Controller
@RequestMapping("/gallery")
@ResponseBody
public class VideoController {


    @Autowired
    private VideoService videoService;


    @RequestMapping("get")
    public String getGallery(String userId){
        List<Video> videoMeta = videoService.getGallery(userId);
        return JSON.toJSONString(videoMeta);
    }

    @RequestMapping("collect")
    public String collectMovie(HttpServletRequest request, String videoId){
        String email = (String) request.getAttribute("userEmail");
        System.out.println(videoId);
        boolean result = videoService.collectVideo(email, videoId);
        if (result) return JSON.toJSONString("success");
        else return JSON.toJSONString("fail");
    }

    @RequestMapping("remove")
    public String removeVideo(HttpServletRequest request, String videoId){

        String email = (String) request.getAttribute("userEmail");
        boolean result = videoService.deleteVideo(email, videoId);
        if (result) return JSON.toJSONString("success");
        else return JSON.toJSONString("fail");
    }
}
