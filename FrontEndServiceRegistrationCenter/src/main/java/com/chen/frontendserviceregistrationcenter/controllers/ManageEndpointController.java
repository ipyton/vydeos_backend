package com.chen.frontendserviceregistrationcenter.controllers;

import com.chen.frontendserviceregistrationcenter.entities.ScaleRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/management")
public class ManageEndpointController {

    @PostMapping("/scale_up")
    public String scaleUp(ScaleRequest request) {
        return "";
    }

}
