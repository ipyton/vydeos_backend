package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.Message;
import com.chen.blogbackend.services.ChatGroupService;
import com.chen.blogbackend.services.FriendsService;
import com.chen.blogbackend.services.SearchService;
import com.chen.blogbackend.services.SingleMessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequestMapping("chat")
@Controller()
@ResponseBody
public class SingleMessageController {

    @Autowired
    SingleMessageService service;

    @Autowired
    ChatGroupService groupService;
    @Autowired
    FriendsService friendsService;

    @Autowired
    SearchService searchService;

    //by single sender.
//    @RequestMapping("get_messages")
//    public List<NotificationMessage> getMessagesByUserId(HttpServletRequest httpServletRequest, @RequestParam(value = "receiverId", required = false) String receiverId,
//                                                         @RequestParam(value = "groupId", required = false) Long groupId, @RequestParam("type") String type,
//                                                         @RequestParam("timestamp") Long timestamp, String pageState){
//        String userId = (String) httpServletRequest.getAttribute("userEmail");
//        System.out.println(groupId);
//        System.out.println(timestamp);
//        System.out.println(receiverId);
//        System.out.println(type);
//        return service.getMessageByUserId(userId, receiverId, type, groupId, timestamp);
//    }

    @RequestMapping("sendMessage")
    public SendingReceipt sendMessage(HttpServletRequest request, String receiverId, Long groupId, String content, String type) throws Exception {
        String senderId = (String) request.getAttribute("userEmail");
        if (type.equals("single")) {
            System.out.println(receiverId);
            System.out.println(content);
            System.out.println(type);
            System.out.println(groupId);
            return service.sendMessage(senderId, receiverId, content, type);
        } else if (type.equals("group")) {
            System.out.println(receiverId);
            System.out.println(content);
            System.out.println(type);
            System.out.println(groupId);
            return groupService.sendGroupMessage(senderId, groupId, content, type);
        }
        return new SendingReceipt(false, -1, -1,-1,true);
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

    @PostMapping("getMessageRecords")
    public Message getMessageRecords(HttpServletRequest request,String userId, String type, Long lastSessionMessageId, Long groupId) {
        String userEmail =(String) request.getAttribute("userEmail");
        if (userId == null || userId.trim().isEmpty()) {
            return new Message(-1, "userId is null");
        }
        if (type == null || type.trim().isEmpty()) {
            return new Message(-1, "type is null");
        }
        if (lastSessionMessageId == null) {
            return new Message(-1, "lastSessionMessageId is null");
        }
        try {
            if (type.equals("single")) {
                List<SingleMessage> newestMessages = service.getSingleMessageRecords(userEmail, userId, lastSessionMessageId);
                return new Message(0, JSON.toJSONString(newestMessages));
            } else if (type.equals("group")) {
                List<GroupMessage> groupMessages = groupService.getGroupMessageRecords(groupId, lastSessionMessageId);
                return new Message(0, JSON.toJSONString(groupMessages));
            } else {
                return new Message(-1, "unsupported type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(-1, "Internal Error");
        }
    }


//    @RequestMapping("recall")
//    public LoginMessage recall(String userId, String receiverId, String messageId) {
//        service.recall(userId, receiverId, messageId);
//        return new LoginMessage(-1, "");
//    }


//    /*
//    * update chat list for a specific pair of users.
//    * */
//    @RequestMapping("getChatRecord")
//    public LoginMessage getChatList(String userId, String friendId) {
//
//    }







    @RequestMapping("registerWebPushEndpoints")
    public LoginMessage registerWebPush(HttpServletRequest request,  @RequestBody WebPushRequest webPushRequest){
        System.out.println("----------------------");
        System.out.println(webPushRequest.getEndpoint());
        System.out.println(webPushRequest.getP256dh());
        System.out.println(webPushRequest.getAuth());

        String email = (String) request.getAttribute("userEmail");
        boolean b = service.addOrUpdateEndpoint(email, webPushRequest.getEndpoint(), webPushRequest.getP256dh(), webPushRequest.getAuth());
        if (!b) {
            return new LoginMessage(-1, "Failed");
        }
        return new LoginMessage(1, "Success");
    }

    @RequestMapping("getWebPushEndpoints")
    public List<String> getWebPushEndpoints(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        if (email == null) {
            return new ArrayList<>();
        }
        return service.getEndpoints(email);
    }
//    @RequestMapping("getRequestCache")
//    public LoginMessage getRequestCache(String userId) {
//
//    }


    @RequestMapping("getUnreadFromAllUsers")
    public Message getNewestMessageFromAllUsers(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userEmail");
        try {
            List<UnreadMessage> unreadMessages = service.getNewestMessagesFromAllUsers(userId);
            return new Message(0, JSON.toJSONString(unreadMessages));
        }
        catch (Exception e) {
            e.printStackTrace();
            return new Message(-1, "An error occurred while fetching newest message");
        }
    }

    @PostMapping("/markUnread")
    public Message markUnread(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userEmail");

        if (userId == null) {
            return new Message(-1, "Insufficient data: userId missing");
        }

        String senderId = (String) body.get("senderId");
        String type = (String) body.get("type");
        Object groupIdObj = body.get("groupId");
        Long groupId = null;

        // groupId 可能是 Integer 类型或 Long 类型或 null
        if (groupIdObj != null) {
            if (groupIdObj instanceof Number) {
                groupId = ((Number) groupIdObj).longValue();
            } else {
                groupId = Long.parseLong(groupIdObj.toString()); // 支持字符串类型
            }
        } else {
            groupId = 0L;
        }

        // 简单的参数校验
        if (type == null || type.isEmpty()) {
            return new Message(-1, "Missing 'type' field");
        }
        if (type.equals("single") &&(senderId == null || senderId.isEmpty())) {
            return new Message(-1, "Missing 'senderId' field");
        }

        if (type.equals("group") && (groupId == null || groupId <= 0)) {
            return new Message(-1, "Invalid 'group' field");
        }


        try {
            boolean result = service.markUnread(userId, senderId, type, groupId);
            if (result) {
                return new Message(0, "Success");
            } else {
                return new Message(-1, "Failed to mark as unread");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(-1, "An error occurred while marking unread");
        }
    }
}
