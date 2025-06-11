package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.Message;
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



    //{name, introduction,userIds,allowInvitesById}
    @PostMapping("create")
    public LoginMessage createGroup(HttpServletRequest req, @RequestParam String name, @RequestParam String introduction,
    @RequestParam List<String> memberIds, @RequestParam Boolean allowInvitesByToken) {
        String email = (String) req.getAttribute("userEmail");

        if (email == null || email.equals("") || name == null || name.equals("")
                || introduction == null || memberIds.size() == 0 || allowInvitesByToken == null) {
            return new LoginMessage(-1, "no sufficient data provided");
        }
        boolean result = service.createGroup(email,name, memberIds,allowInvitesByToken);
        if (result) {
            return new LoginMessage(0, "Success");
        }
        return new LoginMessage(-1, "Fail");
    }

    @PostMapping("join")
    public LoginMessage joinGroup(HttpServletRequest req, Long groupId) {
        String email = (String) req.getAttribute("userEmail");
        boolean result = service.joinGroup(email, groupId);
        if (result) {
            return new LoginMessage(0, "Success");
        }
        return new LoginMessage(-1, "Fail");
    }



    @GetMapping("getDetails")
    public Message getDetail(long groupId) {
        return new Message(0, JSON.toJSONString(service.getGroupDetail(groupId)));
    }

    @GetMapping("updateDetails")
    public Message updateDetail(HttpServletRequest request,long groupId, String name, String description, Boolean allowInviteByToken) {
        String userEmail = (String) request.getAttribute("userEmail");
        boolean result = service.updateGroup(userEmail, groupId, name, description, allowInviteByToken);
        return new Message(0, JSON.toJSONString(result));
    }


    @RequestMapping("quit")
    public LoginMessage quitGroup(String operatorId, String groupId) {
        boolean result = service.quitGroup(operatorId, groupId);
        if (result) {
            return new LoginMessage(0, "Success");
        }
        return new LoginMessage(-1, "Fail");
    }

    @RequestMapping("remove")
    public LoginMessage remove(String operatorId, String groupId, String userID) {
        boolean result = service.removeUser(operatorId, groupId, userID);
        if (result) {
            return new LoginMessage(0, "Success");
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
            return new LoginMessage(0, "Success");
        }
        return new LoginMessage(-1, "Fail");
    }

    @RequestMapping("join_by_invitation")
    public LoginMessage joinGroupByInvitation(String userId,String username, String groupId, String invitationID) {
        boolean result = service.joinByInvitation(userId,username, groupId, invitationID);
        if (result) {
            return new LoginMessage(0, "Success");
        }
        return new LoginMessage(-1, "Fail");
    }

    @RequestMapping("get_groups")
    public LoginMessage getGroups(HttpServletRequest request){
        String userId = (String) request.getAttribute("userEmail");
        if (userId == null || userId.isBlank()) {
            return new LoginMessage(-1, "no sufficient data provided");
        }
        List<GroupUser> groups = service.getGroups(userId);
        return new LoginMessage(0, JSON.toJSONString(groups));
    }

    @GetMapping("get_members")
    public LoginMessage getMembers( long groupId ) {
        List<GroupUser> result = service.getMembers(groupId);
        return new LoginMessage(0, JSON.toJSONString(result));
    }


    @RequestMapping("recall_message")
    public LoginMessage recallMessage(String operatorId, long groupID, String messageId) {
        boolean result = service.recall(operatorId, groupID, messageId);
        return new LoginMessage(-1, "");
    }

}
