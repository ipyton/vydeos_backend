package com.chen.blogbackend.controllers;

import com.chen.blogbackend.entities.FileUploadStatus;
import com.chen.blogbackend.responseMessage.Message;
import com.chen.blogbackend.services.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Controller("movie")
public class FileUploader {

    @Autowired
    FileService fileService;

    //

    @PostMapping("negotiateStep1")
    public FileUploadStatus negotiationStep1(HttpServletRequest request, Long resourceId, String resourceType) {
        return fileService.getUploadStatus(resourceId, resourceType);
    }

    //开始上传
    @PostMapping("uploadFile")
    public FileUploadStatus uploadFile(HttpServletRequest httpServletRequest, @RequestParam("file") MultipartFile file,@RequestParam("type") String type, @RequestParam("currentSlice") Integer currentSlice,
                                       @RequestParam("totalSlice") Integer totalSlice, @RequestParam("resourceId") Long resourceId, @RequestParam("checkSum") String checkSum,@RequestParam("totalChecksum") String totalChecksum ) throws IOException, NoSuchAlgorithmException {
        String email = (String) httpServletRequest.getAttribute("userEmail");
        return fileService.uploadFile(file, type, currentSlice, totalSlice, resourceId, email, checkSum, totalChecksum);
    }


}
