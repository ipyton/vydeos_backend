package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.Video;
import com.chen.blogbackend.services.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/gallery")
@ResponseBody
public class GalleryController {


    @Autowired
    private VideoService videoService;


    @RequestMapping("get")
    public String getGallery(String userId){
        List<Video> videoMeta = videoService.getGallery(userId);
        return JSON.toJSONString(videoMeta);
    }

    @RequestMapping("collect")
    public String collectMovie(HttpServletRequest request, String resourceId, String type, String language){
        //the user should get the language information about this.
        String email = (String) request.getAttribute("userEmail");
        boolean result = videoService.collectVideo(email, resourceId, type, language);
        if (result) return JSON.toJSONString("success");
        else return JSON.toJSONString("fail");
    }

    @RequestMapping("remove")
    public String removeVideo(HttpServletRequest request, String resourceId, String type){

        String email = (String) request.getAttribute("userEmail");
        boolean result = videoService.unstarVideo(email, resourceId, type);
        if (result) return JSON.toJSONString("success");
        else return JSON.toJSONString("fail");
    }
}
