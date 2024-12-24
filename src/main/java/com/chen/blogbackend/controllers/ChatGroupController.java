package com.chen.blogbackend.controllers;

import com.chen.blogbackend.entities.ChatGroup;
import com.chen.blogbackend.entities.ChatGroupMember;
import com.chen.blogbackend.entities.Invitation;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.services.ChatGroupService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequestMapping("group_chat")
@Controller()
@ResponseBody
public class ChatGroupController {

    @Autowired
    ChatGroupService service;


    @PostMapping("create")
    public LoginMessage createGroup(HttpServletRequest req, String groupName ,List<String> users) {
        String email = (String) req.getAttribute("userEmail");
        boolean result = service.createGroup(email,groupName, users);
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



    @PostMapping("getDetail")
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
    public List<ChatGroup> getGroups(String userId, String pagingState){
        List<ChatGroup> result = service.getGroups(userId, pagingState);
        return result;
    }

    @RequestMapping("get_members")
    public LoginMessage getMembers(String userId, String groupId, String pagingState) {
        PagingMessage<ChatGroupMember> result = service.getMembers(userId, groupId, pagingState);
        return new LoginMessage(-1, "");
    }

    @RequestMapping("send_message_group")
    public LoginMessage sendMessage(HttpServletRequest request, String userId,String groupId, String message,String referId,
                                    List<String> objects) {
        boolean result = service.sendMessage(userId, groupId, message, referId, objects);
        return new LoginMessage(-1, "");
    }

    @RequestMapping("recall_message")
    public LoginMessage recallMessage(String operatorId, String groupID, String messageId) {
        boolean result = service.recall(operatorId, groupID, messageId);
        return new LoginMessage(-1, "");
    }

}
