package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.GroupMessage;
import com.chen.blogbackend.entities.SingleMessage;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.services.FriendsService;
import com.chen.blogbackend.services.SearchService;
import com.chen.blogbackend.services.SingleMessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequestMapping("chat")
@Controller()
@ResponseBody
public class SingleMessageController {

    @Autowired
    SingleMessageService service;

    @Autowired
    FriendsService friendsService;

    @Autowired
    SearchService searchService;

    @RequestMapping("get_messages")
    public LoginMessage getMessagesByUserId(HttpServletRequest request, String userId, String receiverId, String pageState){
        service.getMessageByUserId(userId, receiverId,pageState);
        return new LoginMessage(-1, "");
    }

    @RequestMapping("send_message")
    public LoginMessage sendMessage(@RequestBody String singleMessage) {
        SingleMessage singleMessage1 = JSON.parseObject(singleMessage, SingleMessage.class);
        service.sendMessage(singleMessage1);
        return new LoginMessage(-1, "");
    }

    @RequestMapping("block")
    public LoginMessage blockUser(String userId, String receiverId) {
        service.blockUser(userId,receiverId);
        return new LoginMessage(-1, "");
    }

    @RequestMapping("unblock")
    public LoginMessage unblock(String userId, String receiverId) {
        service.unblockUser(userId, receiverId);
        return new LoginMessage(-1, "");

    }

    @RequestMapping("recall")
    public LoginMessage recall(String userId, String receiverId, String messageId) {
        service.recall(userId, receiverId, messageId);
        return new LoginMessage(-1, "");
    }


//    /*
//    * update chat list for a specific pair of users.
//    * */
//    @RequestMapping("getChatRecord")
//    public LoginMessage getChatList(String userId, String friendId) {
//
//    }


    @RequestMapping("getNewestRecords")
    public LoginMessage getNewestRecords(String userId, Long timestamp) {
        List<SingleMessage> newRecords = service.getNewRecords(userId, timestamp);
        return new LoginMessage(1, JSON.toJSONString(newRecords));
    }


//    @RequestMapping("getRequestCache")
//    public LoginMessage getRequestCache(String userId) {
//
//    }
}
