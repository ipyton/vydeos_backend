package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.Message;
import com.chen.blogbackend.services.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
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
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    FileService fileService;
    @Autowired
    private OrderedFormContentFilter formContentFilter;

    @PostMapping("getAvatar/{userEmail}")
    public ResponseEntity<InputStreamResource> getAvatar(@PathVariable String userEmail) {
        return fileService.download("avatar", userEmail, MediaType.IMAGE_JPEG);
    }


    @PostMapping("uploadPostPic")
    public Message uploadPostPic(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        String userEmail = request.getAttribute("userEmail").toString();
        try {
            String s = fileService.uploadPostPics(userEmail, file);
            return new Message(0, s);
        } catch (Exception e) {
            logger.error("An error occurred while processing something", e);
            return new Message(-1, e.getMessage());
        }
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


    @PostMapping("/negotiationStep1")
    public LoginMessage negotiationStep1(HttpServletRequest request,String resourceId, String resourceType,
                                             String wholeHashCode, Long size, String fileName, Integer totalSlice,
                                             Short quality, String format, Integer seasonId, Integer episode) {
        if (resourceId == null || resourceType == null || wholeHashCode == null || size == null ||
                fileName == null || totalSlice == null || quality == null || format == null || seasonId == null
                || episode == null) {
            return new LoginMessage(-1, "Lost parameters");
        }
        String email = (String) request.getAttribute("userEmail");
        return fileService.setUploadStatus(email, resourceId, resourceType,wholeHashCode, size, fileName, totalSlice,
                quality, format,seasonId,episode);
    }

    //开始上传
    @PostMapping("/uploadFile")
    public Message uploadFile(HttpServletRequest httpServletRequest, @RequestParam("file") MultipartFile file, @RequestParam("type") String type, @RequestParam("currentSlice") Integer currentSlice,
                              @RequestParam("resourceId") String resourceId, @RequestParam("hashCode") String checkSum,
                                @RequestParam("seasonId") Integer seasonId, @RequestParam("episode") Integer episode)  {
        if (file == null || type == null || currentSlice == null || resourceId == null || checkSum == null){
            return null;
        }
        String email = (String) httpServletRequest.getAttribute("userEmail");
        int result = -1;
        try {
            result = fileService.uploadFile(file, type, currentSlice, resourceId, email, checkSum, seasonId, episode);
        } catch (Exception e) {
            return new Message(-1, e.getMessage());
        }

        if (result==0) {
            return new Message(0, "success");
        }else if (result == 1) {
            return new Message(result, "is processing");
        }
        return new Message(-1, "Unknown Error");
    }






}
