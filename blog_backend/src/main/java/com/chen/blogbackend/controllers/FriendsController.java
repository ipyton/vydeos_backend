package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.services.AccountService;
import com.chen.blogbackend.services.FriendsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller()
@ResponseBody
@RequestMapping("friends")
public class FriendsController {

    @Autowired
    FriendsService friendsService;

    @Autowired
    AccountService accountService;


    @RequestMapping("getFollowState")
    public LoginMessage getFollowState(String sender, String receiver) throws Exception {
        System.out.println(sender + receiver);
        return new LoginMessage(1, Integer.toString(friendsService.getRelationship(sender, receiver)));
    }


    @RequestMapping("follow")
    public LoginMessage follow(HttpServletRequest request, String receiver, String name) throws Exception {
        String sender = (String) request.getAttribute("userEmail");
        boolean follow = friendsService.follow(sender, receiver, name);
        return new LoginMessage(follow?1:-1,"");
    }

    @RequestMapping("unfollow")
    public LoginMessage unfollow(HttpServletRequest request, String receiver) {
        String sender = (String) request.getAttribute("userEmail");

        boolean result = friendsService.unfollow(sender, receiver);
        return new LoginMessage(result?1:-1, "" );
    }

    @RequestMapping("get_friends")
    public LoginMessage getFriends(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userEmail");
            List<Relationship> friends = friendsService.getFriendsByUserId(userId);
            return new LoginMessage(0, JSON.toJSONString(friends));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LoginMessage(-1, "no sufficient data provided");
    }

    @RequestMapping("get_followers")
    public LoginMessage getFollowers(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userEmail");
        List<Relationship> followersByUserId = friendsService.getFollowersByUserId(userId);
        return new LoginMessage(1, JSON.toJSONString(followersByUserId));
    }

    @RequestMapping("get_idols")
    public LoginMessage getIdols(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userEmail");
        return new LoginMessage(1,JSON.toJSONString(friendsService.getIdolsByUserId(userId)));
    }
//
//    @RequestMapping("get_groups")
//    public LoginMessage getGroups(HttpServletRequest request) {
//        String userId = (String) request.getAttribute("userEmail");
//        return new LoginMessage(1, JSON.toJSONString(friendsService.getGroupById(userId)));
//    }
//
//    @RequestMapping("get_group_users")
//    public PagingMessage<Friend> getGroupFriends(String userId,String groupId) {
//        List<Friend> friendIdsByGroupId = friendsService.getFriendsByGroupId(userId, groupId);
//        PagingMessage<Friend> message = new PagingMessage<>();
//        message.items = friendIdsByGroupId;
//        return message;
//    }
//
//
//    @RequestMapping("move_to_group")
//    public LoginMessage moveTo(String userId, String friendId,String groupId) {
//        boolean b = friendsService.moveToGroup(userId, friendId, groupId);
//
//        return new LoginMessage(-1, "");
//    }
//
//    @RequestMapping("create_group")
//    public LoginMessage createGroup(HttpServletRequest request, UserGroup group) {
//        boolean result = friendsService.createGroup(group);
//        if (result) {
//            return new LoginMessage(1, "SUCCESS");
//        }
//        return new LoginMessage(-1, "FAILED");
//    }
//
//
//    @RequestMapping("remove_group")
//    public LoginMessage removeGroup(String userId, String group) {
//        boolean result = friendsService.removeGroup(group);
//        if (result) {
//            return new LoginMessage(1, "");
//        }
//        else {
//            return new LoginMessage(-1, " ");
//        }
//    }
//
//    @RequestMapping("delete_from_group")
//    public LoginMessage deleteFromGroup(String user, String usrToRemove, String groupFrom) {
//        boolean result = friendsService.deleteFromGroup(user, usrToRemove, groupFrom);
//        return new LoginMessage(-1, "");
//    }

    //get user introduction from searching/friend list
    @RequestMapping("/getUserIntro")
    public LoginMessage getIntro(HttpServletRequest request,String userIdToFollow) throws Exception {
        String userId = (String) request.getAttribute("userEmail");
        Account friendDetailsById = accountService.getFriendDetailsById(userId, userIdToFollow);
        if (null == friendDetailsById ) {
            return new LoginMessage(-1, null);
        }
        System.out.println(JSON.toJSONString(friendDetailsById));
        return new LoginMessage(1,JSON.toJSONString(friendDetailsById));
    }

    //This is a method to call if user doesn't have local messages.
    @PostMapping("getFriendsToken")
    public LoginMessage getFriendsToken(String user) {
        //service.getRelationship()
        return new LoginMessage(1, "success");
    }


    @PostMapping("get_invitation_code")
    public LoginMessage getInvitationCode(String userId) {
        List<Invitation> invitations = friendsService.getInvitations(userId);
        return new LoginMessage(1, JSON.toJSONString(invitations));
    }

    @PostMapping("verify_invitation_code")
    public LoginMessage verifyInvitationCode(Invitation invitation) {
        boolean result = friendsService.verifyInvitation(invitation);
        if (result) {
            return new LoginMessage(1, "failed");
        }
        return new LoginMessage(-1, "success");

    }

}
