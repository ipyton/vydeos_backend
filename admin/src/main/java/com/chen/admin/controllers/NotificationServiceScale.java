package com.chen.admin.controllers;

import com.chen.admin.services.ScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("admin/scale")

public class NotificationServiceScale {

    @Autowired
    ScaleService scaleService;


    @PostMapping("up")
    @ResponseBody
    public String scale(List<String> partitions) throws ExecutionException, InterruptedException {
        scaleService.scaleNotificationPartitions(partitions);
        return "success";
    }

}
