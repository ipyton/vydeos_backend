package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.services.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@ResponseBody
@Controller()
@RequestMapping("file")
public class FileController {

    @Autowired
    FileService fileService;

    @PostMapping("getAvatar/{userEmail}")
    public ResponseEntity<InputStreamResource> getAvatar(@PathVariable String userEmail) {
        return fileService.download("avatar", userEmail, MediaType.IMAGE_JPEG);
    }

    @PostMapping("uploadAvatar")
    public String uploadAvatar(HttpServletRequest httpServletRequest, MultipartFile file) {
        String userEmail = httpServletRequest.getHeader("useremail");
        return JSON.toJSONString(fileService.uploadAvatar(userEmail, file));
    }


    @PostMapping("uploadPostPic")
    public ResponseEntity<String> uploadPostPic(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        String userEmail = request.getAttribute("userEmail").toString();
        return fileService.uploadPostPics(userEmail, file);
    }

    @PostMapping("downloadPostPic/{filename}")
    public ResponseEntity<InputStreamResource> downloadPostPic(@PathVariable String filename) {
        return fileService.download("postpics",filename, MediaType.IMAGE_JPEG);

    }

    @PostMapping("uploadChatPics/")
    public String uploadChatPics(String userId,  @RequestParam("file")  MultipartFile file) {
        return JSON.toJSONString(fileService.uploadChatPics(userId, file));
    }

    @GetMapping("downloadChatPics/{filename}")
    public ResponseEntity<InputStreamResource> downloadChatPics(@PathVariable String filename) {
        return fileService.download("chatpics",filename, MediaType.IMAGE_JPEG);
    }

    @PostMapping("uploadChatVoice/")
    public String uploadChatVoice(String userId,  @RequestParam("file") MultipartFile file) {
        return JSON.toJSONString(fileService.uploadChatVoice(userId, file));
    }

    @PostMapping("downloadChatVoice/{resourceId}")
    public ResponseEntity<InputStreamResource> downloadChatVoice(@PathVariable String resourceId) {
        return fileService.download("chatvoice",resourceId ,new MediaType("audio/mpeg"));
    }

    @PostMapping("uploadPostVoice")
    public String uploadPostVoice(String userId, MultipartFile file) {
        return JSON.toJSONString(fileService.uploadPostVoice(userId, file));
    }

    @PostMapping("downloadPostVoice/{resourceId}")
    public ResponseEntity<InputStreamResource> downloadPostVoice(@PathVariable String resourceId)
    {
        return fileService.download("postvoice", resourceId, new MediaType("audio/mpeg"));
    }


    @PostMapping("uploadChatVideo")
    public ResponseEntity<String> uploadChatVideo(String userId,  @RequestParam("file") MultipartFile file) {
        return fileService.uploadChatVideo(userId, file);
    }

    @PostMapping("downloadChatVideo/{resourceId}")
    public ResponseEntity<InputStreamResource> downloadChaVideo(@PathVariable String resourceId ) {
        return fileService.download("chatvideo",resourceId,new MediaType("video/mpeg"));
    }




}
