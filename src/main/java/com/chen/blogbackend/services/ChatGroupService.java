package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.ChatGroupDao;
import com.chen.blogbackend.DAO.FriendDao;
import com.chen.blogbackend.DAO.InvitationDao;
import com.chen.blogbackend.entities.ChatGroup;
import com.chen.blogbackend.entities.ChatGroupMember;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.Invitation;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.util.StringUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.*;
import jakarta.annotation.PostConstruct;
import jnr.ffi.annotations.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Service;

import java.awt.event.TextEvent;
import java.util.Date;
import java.util.List;

@Service
public class ChatGroupService {

    @Autowired
    CqlSession session;

    @Autowired
    PictureService service;

    PreparedStatement insertChatGroupById;
    PreparedStatement insertChatGroupByUser;
    PreparedStatement insertChatRecordById;

    PreparedStatement getGroups;
    PreparedStatement getMembers;
    PreparedStatement getMembersId;


    PreparedStatement delChatRecordByID;
    PreparedStatement delChatGroupById;
    PreparedStatement delChatGroupByUser;
    PreparedStatement delAllChatGroupById;

    PreparedStatement truncateChatGroupById;
    PreparedStatement truncateChatGroupByUser;
    PreparedStatement getRecord;

    PreparedStatement recall;
    //generate here.
    InvitationDao invitationDao;
    ChatGroupDao chatGroupDao;
    FriendDao friendDao;


    @PostConstruct
    public void init() {

        insertChatGroupById = session.prepare("");
        insertChatGroupByUser = session.prepare("");
        insertChatRecordById = session.prepare("");


        delChatRecordByID = session.prepare("");
        delChatGroupById = session.prepare("");
        delChatGroupByUser = session.prepare("");
        truncateChatGroupById = session.prepare("");
        getGroups = session.prepare("");
        getMembers = session.prepare("");
        recall = session.prepare("");
        chatGroupDao = null;
    }

    public boolean joinGroup(String userId, String groupId) {
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        builder.addStatements(insertChatGroupById.bind(userId, groupId),
                insertChatGroupByUser.bind(groupId, userId));
        ResultSet execute = session.execute(builder.build());
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public boolean quitGroup(String userId, String groupId) {
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        builder.addStatements(delChatGroupById.bind(userId, groupId),
                delChatGroupByUser.bind(groupId, userId));
        ResultSet execute = session.execute(builder.build());
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public Invitation generateInvitation(String operator, String userId, String groupId) {
        String invitationId = userId + System.currentTimeMillis() + StringUtil.generateRandomString(10);
        Invitation invitation = new Invitation(groupId, (new Date(System.currentTimeMillis() + 360000)), userId, 10);
        invitationDao.insert(invitation);
        return invitation;
    }

    public boolean dismissGroup(String operatorId, String groupId) {
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        ResultSet execute = session.execute(getMembersId.bind(groupId));
        for (Row row: execute.all()) {
            String userId = row.get("user_id", String.class);
            builder.addStatement(delChatGroupByUser.bind(groupId, userId));
        }
        builder.addStatement(delAllChatGroupById.bind(groupId));
        ResultSet execute1 = session.execute(builder.build());
        return execute1.getExecutionInfo().getErrors().size() == 0;
    }

    public boolean joinByInvitation(String userId, String username, String groupId, String invitationID) {
        Invitation select = invitationDao.select(invitationID);
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        builder.addStatements(insertChatGroupById.bind(groupId, userId, userId),insertChatGroupByUser.bind(userId, groupId));
        if (select.getGroupId().equals(groupId) && System.currentTimeMillis() < select.getExpire_time().getTime()) {

            return true;
        }
    return false;
    }

    public PagingMessage<ChatGroupMember> getMembers(String userId, String groupId) {
        ResultSet execute = session.execute(getMembers.bind(groupId));

    }

    public boolean sendMessage(String userId, String groupId, String message, String referId, List<String> objects) {
        ResultSet execute = session.execute(insertChatRecordById.bind(groupId, StringUtil.generateRandomString(10), userId,
                message, referId, new Date(System.currentTimeMillis()), objects, false));
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public boolean permitted(String operatorId, String groupId, Stru) {

    }

    public boolean recall(String operatorId, String groupID, String messageId) {
        ResultSet execute = session.execute(getRecord.bind(groupID, messageId));
        if (execute.getColumnDefinitions().get("name") == operatorId) {
            ResultSet execute1 = session.execute(delChatRecordByID.bind(messageId));
            return true;
        }
        return false;
    }

    public boolean removeUser(String operatorId, String groupId, String userId) {
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        builder.addStatements(delChatGroupById.bind(userId, groupId),
                delChatGroupByUser.bind(groupId, userId));
        ResultSet execute = session.execute(builder.build());
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public PagingMessage<ChatGroup> getGroups(String userId, String pagingState) {
        ResultSet execute = session.execute(getGroups.bind(userId).setPagingState(PagingState.fromString(pagingState)));
        PagingIterable<ChatGroup> groups = chatGroupDao.convert(execute);
        return new PagingMessage<>(groups.all(), execute.getExecutionInfo().getPagingState().toString(), -1);
    }
}
