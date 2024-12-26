package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.services.ChatGroupService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("group_chat")
@Controller()
@ResponseBody
public class ChatGroupController {




    @Autowired
    ChatGroupService service;


    @PostMapping("create")
    public LoginMessage createGroup(HttpServletRequest req, @RequestBody() GroupRequest groupRequest) {
        String email = (String) req.getAttribute("userEmail");
        String groupName = groupRequest.getGroupName();
        List<String> members = groupRequest.getMembers();
        if (email == null || email.equals("") || groupName == null || groupName.equals("") || members == null || members.size() == 0) {
            return new LoginMessage(-1, "no sufficient data provided");
        }
        boolean result = service.createGroup(email,groupName, members);
        if (result) {
            return new LoginMessage(1, "Success");
        }
        return new LoginMessage(-1, "Fail");
    }

    @PostMapping("join")
    public LoginMessage joinGroup(HttpServletRequest req, String groupId) {
        String email = (String) req.getAttribute("userEmail");
        boolean result = service.joinGroup(email, groupId);
        if (result) {
            return new LoginMessage(1, "Success");
        }
        return new LoginMessage(-1, "Fail");
    }



    @GetMapping("getDetail")
    public ChatGroup getDetail(long groupId) {
        return service.getGroupDetail(groupId);
    }



    @RequestMapping("quit")
    public LoginMessage quitGroup(String operatorId, String groupId) {
        boolean result = service.quitGroup(operatorId, groupId);
        if (result) {
            return new LoginMessage(1, "Success");
        }
        return new LoginMessage(-1, "Fail");
    }

    @RequestMapping("remove")
    public LoginMessage remove(String operatorId, String groupId, String userID) {
        boolean result = service.removeUser(operatorId, groupId, userID);
        if (result) {
            return new LoginMessage(1, "Success");
        }
        return new LoginMessage(-1, "Fail");
    }

    @RequestMapping("invite")
    public LoginMessage makeInvitation(String operator, String userId, String groupId) {
        Invitation result = service.generateInvitation(operator, userId, groupId);
        return new LoginMessage(-1, "");
    }

    @RequestMapping("dismiss")
    public LoginMessage dismissGroup(String operatorId, String groupId) {
        boolean result = service.dismissGroup(operatorId, groupId);
        if (result) {
            return new LoginMessage(1, "Success");
        }
        return new LoginMessage(-1, "Fail");
    }

    @RequestMapping("join_by_invitation")
    public LoginMessage joinGroupByInvitation(String userId,String username, String groupId, String invitationID) {
        boolean result = service.joinByInvitation(userId,username, groupId, invitationID);
        if (result) {
            return new LoginMessage(1, "Success");
        }
        return new LoginMessage(-1, "Fail");
    }

    @RequestMapping("get_groups")
    public LoginMessage getGroups(HttpServletRequest request){
        String userId = (String) request.getAttribute("userEmail");
        if (userId == null || userId.isBlank()) {
            return new LoginMessage(-1, "no sufficient data provided");
        }
        List<GroupUser> groups = service.getGroups(userId, null);
        return new LoginMessage(1, JSON.toJSONString(groups));
    }

    @GetMapping("get_members")
    public LoginMessage getMembers(String userId, long groupId, String pagingState) {
        List<GroupUser> result = service.getMembers(userId, groupId, pagingState);
        return new LoginMessage(-1, JSON.toJSONString(result));
    }

    @RequestMapping("send_message_group")
    public LoginMessage sendMessage(HttpServletRequest request, String userId,String groupId, String message,String referId,
                                    List<String> objects) {
        boolean result = service.sendMessage(userId, groupId, message, referId, objects);
        return new LoginMessage(-1, "");
    }

    @RequestMapping("recall_message")
    public LoginMessage recallMessage(String operatorId, long groupID, String messageId) {
        boolean result = service.recall(operatorId, groupID, messageId);
        return new LoginMessage(-1, "");
    }

}
