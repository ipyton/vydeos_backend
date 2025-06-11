package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.InvitationDao;
import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.mappers.GroupParser;
import com.chen.blogbackend.mappers.MessageParser;
import com.chen.blogbackend.util.RandomUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ChatGroupService {

    @Autowired
    CqlSession session;

    @Autowired
    PictureService service;

    @Autowired
    KeyService keyService;

    @Autowired
    NotificationProducer notificationProducer;

    PreparedStatement getGroups;
    PreparedStatement getMembers;
    PreparedStatement getMembersId;


    PreparedStatement removeMember;
    PreparedStatement delAllChatGroupById;

    PreparedStatement getGroupDetails;
    PreparedStatement updateGroupDetails;
    PreparedStatement getRecordByGroupId;


    PreparedStatement createChatGroup;
    PreparedStatement insertGroupMemberByUser;
    PreparedStatement insertGroupMemberByGroup;

    PreparedStatement getGroupMember;
    PreparedStatement getGroupOwner;

    //generate here.
    InvitationDao invitationDao;




    @PostConstruct
    public void init() {

        //insertChatRecordById = session.prepare("insert into group_chat.group_chat_record_by_id (group_id , message_id ,type ,  user_id  ,content , referUserID , referMessageId , send_time, media, recall) values(?,?,?,?,?,?,?,?,?,?)");
        getGroupDetails = session.prepare("select * from group_chat.chat_group_details where group_id = ?");
        removeMember = session.prepare("delete from group_chat.chat_group_members where group_id = ? and user_id = ?");
        createChatGroup = session.prepare("insert into group_chat.chat_group_details (group_id, avatar, config, introduction, name, owner_id,create_time,allow_invite_by_token) values(?, ?, ?, ?, ?, ?, ?, ?)");
        getGroups = session.prepare("select * from group_chat.chat_group_members_by_user where user_id = ?");
        getMembers = session.prepare("select * from group_chat.chat_group_members_by_group where group_id = ? ");
        //getRecord = session.prepare("select * from group_chat.group_chat_record_by_id where group_id = ? and message_id = ?");
        //recall = session.prepare("delete from group_chat.group_chat_record_by_id where group_id = ? and message_id = ?");
        getRecordByGroupId = session.prepare("select * from chat.group_chat_records where group_id= ? and session_message_id > ? limit 10");
        //getRecordByMemberId = session.prepare("select * from chat.chat_messages_mailbox where user_id= ? ");
        insertGroupMemberByUser = session.prepare("insert into group_chat.chat_group_members_by_user (group_id, user_id, group_name) values (?, ?, ?)");
        insertGroupMemberByGroup = session.prepare("insert into group_chat.chat_group_members_by_group (group_id, user_id, user_name) values (?, ?, ?)");
        getGroupMember = session.prepare("select * from group_chat.chat_group_members where group_id = ? and user_id = ?");
        getGroupOwner = session.prepare("select ownerId from group_chat.chat_group_details where group_id = ?;");
        updateGroupDetails = session.prepare("UPDATE group_chat.chat_group_details SET introduction = ?, name = ?, allow_invite_by_token = ? WHERE group_id = ?;");

    }
    public boolean joinGroup(String userId, Long groupId) {
        return joinGroup(userId, groupId, "");
    }

    public boolean joinGroup(String userId, Long groupId,String groupName) {
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        builder.addStatements(insertGroupMemberByGroup.bind(groupId, userId, ""));
        builder.addStatements(insertGroupMemberByUser.bind(groupId, userId, groupName ));
        ResultSet execute = session.execute(builder.build());
        return execute.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean quitGroup(String userId, String groupId) {
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        builder.addStatements(removeMember.bind(groupId, userId));
        ResultSet execute = session.execute(builder.build());
        return execute.getExecutionInfo().getErrors().isEmpty();
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
            builder.addStatement(removeMember.bind(groupId, userId));
        }
        builder.addStatement(delAllChatGroupById.bind(groupId));
        ResultSet execute1 = session.execute(builder.build());
        return execute1.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean joinByInvitation(String userId, String username, String groupId, String invitationID) {
        Invitation select = invitationDao.select(invitationID);
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        builder.addStatements(insertGroupMemberByUser.bind(groupId, userId, username));
        return select.getReceiverId().equals(groupId) && System.currentTimeMillis() < select.getExpire_time().getTime();
    }

    public List<GroupUser> getMembers(long groupId) {
        ResultSet execute = session.execute(getMembers.bind(groupId));
        return GroupParser.groupListParser(execute);
    }


    public List<SingleMessage> getGroupMessageByGroupIDAndTimestamp(Long groupId, long timestamp) {
        ResultSet execute = session.execute(getRecordByGroupId.bind(groupId, Instant.ofEpochMilli(timestamp)));
        return MessageParser.parseToNotificationMessage(execute);
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
        builder.addStatements(removeMember.bind(userId, groupId));
        ResultSet execute = session.execute(builder.build());
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public List<GroupUser> getGroups(String userId) {
        System.out.println(userId);
        ResultSet execute = session.execute(getGroups.bind(userId));
        return GroupParser.groupListParser(execute);
    }

    public boolean recall(String operatorId, long groupID, String messageId) {
        return true;
    }

    public boolean createGroup(String ownerId, String groupName, List<String> members, Boolean allowInvitesByToken) {
        long groupId = keyService.getIntKey("chatGroup"); // 你需要实现如何生成群组 ID

        boolean result = true;
        members.add(ownerId);
        for (String memberId : members) {
            result = joinGroup(memberId, groupId, groupName);
            if (!result) {
                return false;
            }
        }
        ResultSet execute = session.execute(createChatGroup.bind(groupId, "", new HashMap<>(), "", groupName, ownerId, Instant.now(),allowInvitesByToken));
        if (!execute.getExecutionInfo().getErrors().isEmpty()) {
            return false;
        }
        return true; // 如果没有异常发生，返回 true
    }

    public boolean isInGroup(String userId, long groupId) {
        ResultSet execute = session.execute(getGroupMember.bind(groupId, userId));
        if (execute.getExecutionInfo().getErrors().isEmpty()) {
            if (execute.all().isEmpty()) {
                return false;
            }
            return true;
        }
        return false;
    }

    public SendingReceipt sendGroupMessage(String userId, long groupId, String content, String messageType) throws Exception {
        Instant now = Instant.now();
        SendingReceipt receipt = new SendingReceipt();


        receipt.setMessageId(keyService.getLongKey("chat_global"));
        receipt.setSessionMessageId(keyService.getLongKey("group_chat_" + groupId));

        GroupMessage groupMessage = new GroupMessage(userId, groupId, receipt.getMessageId(), content, messageType, Instant.now(), "group", -1,new ArrayList<>(),false , receipt.getSessionMessageId());
        if (isInGroup(userId, groupId)) {
            receipt.setMessageId(keyService.getIntKey("groupMessage"));
            groupMessage.setMessageId(receipt.getMessageId());
            receipt.setResult(true);
            receipt.setTimestamp(now.toEpochMilli());
            notificationProducer.sendNotification(groupMessage);
            return receipt;
        }

        receipt.setResult(false);
        return receipt;
    }

    public ChatGroup getGroupDetail(long groupId) {
        Select select = QueryBuilder.selectFrom("group_chat","chat_group_details").all()
                .whereColumn("group_id").isEqualTo(QueryBuilder.literal(groupId));

        SimpleStatement build = select.build();

        // 执行查询并获取结果
        return GroupParser.parseDetails(session.execute(build));
    }

    public List<SingleMessage> getNewestMessages(String userId, long timestamp) {
        ResultSet execute = session.execute(getGroups.bind(userId));
        List<SingleMessage> singleMessages = new ArrayList<>();
        if (!execute.getExecutionInfo().getErrors().isEmpty()) {
            return singleMessages;
        }
        List<Row> all = execute.all();
        for (Row row : all) {
            Long groupId = row.getLong("group_id");
            List<SingleMessage> groupMessageByGroupID = getGroupMessageByGroupIDAndTimestamp(groupId, timestamp);
            singleMessages.addAll(groupMessageByGroupID);

        }
        return singleMessages;
    }

    public List<GroupMessage> getGroupMessageRecords(Long groupId, Long lastSessionMessageId) {
        return null;
    }

    public boolean updateGroup(String userEmail,long groupId, String name, String description, Boolean allowInviteByToken) {
        ResultSet execute = session.execute(getGroups.bind(groupId));
        Row one = execute.one();
        if (one == null) {
            return false;
        }
        else {
            String ownerId = one.getString("ownerId");
            if (!ownerId.equals(userEmail)) {
                return false;
            }
        }
        session.execute(updateGroupDetails.bind( description,name, allowInviteByToken,groupId));
        return true;
    }


//    @RequestMapping("setRequestCache")
//    public LoginMessage setRequestCache(GroupMessage message) {
//
//    }
}
