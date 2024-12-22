package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.ChatGroupDao;
import com.chen.blogbackend.DAO.ChatGroupMemberDao;
import com.chen.blogbackend.DAO.FriendDao;
import com.chen.blogbackend.DAO.InvitationDao;
import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.mappers.MessageParser;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.util.RandomUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

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


    PreparedStatement delChatGroupById;
    PreparedStatement delChatGroupByUser;
    PreparedStatement delAllChatGroupById;

    PreparedStatement truncateChatGroupById;
    PreparedStatement getRecord;
    PreparedStatement getGroupDetails;
    PreparedStatement getRecordByGroupId;
    PreparedStatement getRecordByMemberId;
    //PreparedStatement setChatRecordCache;

    //generate here.
    InvitationDao invitationDao;
    ChatGroupDao chatGroupDao;
    FriendDao friendDao;
    ChatGroupMemberDao chatGroupMemberDao;




    @PostConstruct
    public void init() {

        insertChatGroupById = session.prepare("insert into group_chat.chat_group_by_id (group_id, user_id, user_name) values(?,?,?)");
        insertChatGroupByUser = session.prepare("insert into group_chat.chat_group_by_user (user_id, group_id, group_name) values(?,?,?)");
        insertChatRecordById = session.prepare("insert into group_chat.group_chat_record_by_id (group_id , message_id ,type ,  user_id  ,content , referUserID , referMessageId , send_time, media, recall) values(?,?,?,?,?,?,?,?,?,?)");
        getGroupDetails = session.prepare("select * from group_chat.chat_group_details where group_id = ?");
        delChatGroupById = session.prepare("delete from group_chat.chat_group_by_id where group_id = ? and user_id = ?");
        delChatGroupByUser = session.prepare("delete from group_chat.chat_group_by_user where user_id = ? and group_id = ?");
        truncateChatGroupById = session.prepare("delete from group_chat.chat_group_by_id where group_id = ? and user_id = ?");
        getGroups = session.prepare("select * from group_chat.chat_group_by_user where user_id = ?");
        getMembers = session.prepare("select * from group_chat.chat_group_by_id where group_id = ? ");
        //getRecord = session.prepare("select * from group_chat.group_chat_record_by_id where group_id = ? and message_id = ?");
        //recall = session.prepare("delete from group_chat.group_chat_record_by_id where group_id = ? and message_id = ?");
        chatGroupDao = null;
        getRecordByGroupId = session.prepare("select * from group_chat.group_chat_record_by_id where group_id= ? and message_id > ?");
        getRecordByMemberId = session.prepare("select * from group_chat.chat_messages_mailbox where user_id= ? ");
    }
    public List<OnlineGroupMessage> getOnlineGroupMessageByUserID(long userID) {
        ResultSet execute = session.execute(getRecordByMemberId.bind(userID));
        List<OnlineGroupMessage> onlineGroupMessages = MessageParser.parseToOnlineGroupMessage(execute);
        return onlineGroupMessages;
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
        builder.addStatements(delChatGroupById.bind(groupId, userId),
                delChatGroupByUser.bind(userId, groupId));
        ResultSet execute = session.execute(builder.build());
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public Invitation generateInvitation(String operator, String userId, String groupId) {
        String invitationId = userId + System.currentTimeMillis() + RandomUtil.generateRandomString(10);
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
        return select.getGroupId().equals(groupId) && System.currentTimeMillis() < select.getExpire_time().getTime();
    }

    public PagingMessage<ChatGroupMember> getMembers(String userId, String groupId, String pagingState) {
        ResultSet execute = session.execute(getMembers.bind(groupId));
        PagingIterable<ChatGroupMember> convert = chatGroupMemberDao.convert(execute);
        return new PagingMessage<>(convert.all(), pagingState, -1);
    }

    public boolean sendMessage(String userId, String groupId, String message, String referId, List<String> objects) {
        ResultSet execute = session.execute(insertChatRecordById.bind(groupId, RandomUtil.generateRandomString(10), userId,
                message, referId, new Date(System.currentTimeMillis()), objects, false));
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public List<GroupMessage> getGroupMessageByGroupID(String groupId) {
        ResultSet execute = session.execute(getRecordByGroupId.bind(groupId));
        return MessageParser.parseToGroupMessage(execute);
    }

//    public boolean recall(String operatorId, String groupID, String messageId) {
//        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.LOGGED);
//
//        BatchStatementBuilder batchStatementBuilder = builder.addStatements(getRecord.bind(groupID, messageId), getGroupDetails.bind(groupID));
//
//        ResultSet execute = session.execute(batchStatementBuilder.build());
//        String userId = execute.all().get(0).get("user_id", String.class);
//        String ownerId = execute.all().get(1).get("owner_id", String.class);
//        if (null == userId || null == ownerId) {
//            return false;
//        }
//        if (userId.equals(operatorId) || ownerId.equals(operatorId)) {
//            ResultSet execute1 = session.execute(recall.bind(messageId));
//            return true;
//        }
//        return false;
//    }

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

    public boolean recall(String operatorId, String groupID, String messageId) {
        return true;
    }


//    @RequestMapping("setRequestCache")
//    public LoginMessage setRequestCache(GroupMessage message) {
//
//    }
}
