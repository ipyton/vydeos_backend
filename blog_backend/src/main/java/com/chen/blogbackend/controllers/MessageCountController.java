package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.MessageCountEntity;
import com.chen.blogbackend.services.MessageCountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller("/count")
@RestController
public class MessageCountController {

    @Autowired
    MessageCountService messageCountService;

    @RequestMapping("count")
    @ResponseBody
    public String getMessageCount(HttpServletRequest request){
        String userid = (String) request.getAttribute("userid");
        String type = (String) request.getAttribute("type");
        if (type.equals("group")) {
            String groupid = (String) request.getAttribute("groupid");
            MessageCountEntity groupChatCount = messageCountService.getGroupChatCount(groupid, userid);
            return JSON.toJSONString(groupChatCount);
        }
        else if (type.equals("user")) {
            String fromUserId = (String) request.getAttribute("from_user_id");
            MessageCountEntity singleMessageCount = messageCountService.getSingleMessageCount(fromUserId, userid);
            return JSON.toJSONString(singleMessageCount);
        }
        else if (type.equals("total")) {
            MessageCountEntity totalMessageCount = messageCountService.getTotalMessageCount(userid);
            return JSON.toJSONString(totalMessageCount);
        }
        return null;
    }



}
