package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.ChatGroupDao;
import com.chen.blogbackend.DAO.FriendDao;
import com.chen.blogbackend.DAO.InvitationDao;
import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.entities.deprecated.GroupMessage;
import com.chen.blogbackend.mappers.GroupUserParser;
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


    PreparedStatement insertChatRecordById;

    PreparedStatement getGroups;
    PreparedStatement getMembers;
    PreparedStatement getMembersId;


    PreparedStatement removeMember;
    PreparedStatement delAllChatGroupById;

    PreparedStatement getRecord;
    PreparedStatement getGroupDetails;
    PreparedStatement getRecordByGroupId;
    PreparedStatement getRecordByMemberId;
    //PreparedStatement setChatRecordCache;
    PreparedStatement createChatGroup;
    PreparedStatement insertGroupMember;
    PreparedStatement getGroupMember;

    //generate here.
    InvitationDao invitationDao;




    @PostConstruct
    public void init() {

        insertChatRecordById = session.prepare("insert into group_chat.group_chat_record_by_id (group_id , message_id ,type ,  user_id  ,content , referUserID , referMessageId , send_time, media, recall) values(?,?,?,?,?,?,?,?,?,?)");
        getGroupDetails = session.prepare("select * from group_chat.chat_group_details where group_id = ?");
        removeMember = session.prepare("delete from group_chat.chat_group_members where group_id = ? and user_id = ?");
        createChatGroup = session.prepare("insert into group_chat.chat_group_details (group_id, avatar, config, group_description, group_name, owner,create_time) values(?, ?, ?, ?, ?, ?, ?)");
        getGroups = session.prepare("select * from group_chat.chat_group_members where user_id = ?");
        getMembers = session.prepare("select * from group_chat.chat_group_members where group_id = ? ");
        //getRecord = session.prepare("select * from group_chat.group_chat_record_by_id where group_id = ? and message_id = ?");
        //recall = session.prepare("delete from group_chat.group_chat_record_by_id where group_id = ? and message_id = ?");
        getRecordByGroupId = session.prepare("select * from group_chat.group_chat_record_by_id where group_id= ? and message_id > ?");
        getRecordByMemberId = session.prepare("select * from group_chat.chat_messages_mailbox where user_id= ? ");
        insertGroupMember = session.prepare("insert into group_chat.chat_group_members (group_id, user_id, user_name, group_name) values (?, ?, ?, ?)");
        getGroupMember = session.prepare("select * from group_chat.chat_group_members where group_id = ? and user_id = ?");
    }
    public List<OnlineGroupMessage> getOnlineGroupMessageByUserID(long userID) {
        ResultSet execute = session.execute(getRecordByMemberId.bind(userID));
        return MessageParser.parseToOnlineGroupMessage(execute);
    }

    public boolean joinGroup(String userId, String groupId) {
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        builder.addStatements(insertGroupMember.bind(groupId, userId, "", ""));
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
        builder.addStatements(insertGroupMember.bind(groupId, userId, username,""));
        return select.getReceiverId().equals(groupId) && System.currentTimeMillis() < select.getExpire_time().getTime();
    }

    public List<GroupUser> getMembers(String userId, long groupId, String pagingState) {
        ResultSet execute = session.execute(getMembers.bind(groupId));
        return GroupUserParser.groupUserParser(execute);

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
        builder.addStatements(removeMember.bind(userId, groupId));
        ResultSet execute = session.execute(builder.build());
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public List<GroupUser> getGroups(String userId, String pagingState) {
        System.out.println(userId);
        ResultSet execute = session.execute(getGroups.bind(userId));
        return GroupUserParser.groupUserParser(execute);
    }

    public boolean recall(String operatorId, long groupID, String messageId) {
        return true;
    }

    public boolean createGroup(String creatorId, String groupName, List<String> members) {
        long groupId = keyService.getIntKey("chatGroup"); // 你需要实现如何生成群组 ID

        boolean result = true;
        members.add(creatorId);
        for (String memberId : members) {
            ResultSet execute = session.execute(insertGroupMember.bind(groupId, memberId, "", groupName));
            int size = execute.getExecutionInfo().getErrors().size();
            if (size != 0) {
                return false;
            }
        }
//        createChatGroup = session.prepare("insert into group_chat.chat_group_details (group_id, avatar, config, group_description, group_name, owner) values(?, ?, ?, ?, ?, ?)");
        ResultSet execute = session.execute(createChatGroup.bind(groupId, "", new HashMap<>(), "", groupName, creatorId, Instant.now()));
        if (!execute.getExecutionInfo().getErrors().isEmpty()) {
            return false;
        }

        return result; // 如果没有异常发生，返回 true
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
        NotificationMessage singleMessage =  new NotificationMessage(null, userId, "",groupId, null, null, "group", content, -1, now,-1);
        if (isInGroup(userId, groupId)) {
            receipt.sequenceId = keyService.getIntKey("groupMessage");
            receipt.result = true;
            notificationProducer.sendNotification(singleMessage);
            return receipt;
        }

        receipt.result = false;
        return receipt;
    }

    public ChatGroup getGroupDetail(long groupId) {
        Select select = QueryBuilder.selectFrom("group_chat","chat_group_details").all()
                .whereColumn("group_id").isEqualTo(QueryBuilder.literal(groupId));

        SimpleStatement build = select.build();

        // 执行查询并获取结果
        Row row = session.execute(build).one();

        if (row != null) {
            // 映射查询结果到 ChatGroupDetails 对象
            long groupIdLong = row.getLong("group_id");
            String groupName = row.getString("group_name");
            String groupDescription = row.getString("group_description");
            String owner = row.getString("owner");
            Map<String, String> config = row.getMap("config", String.class, String.class);
            String avatar = row.getString("avatar");
            //Instant createDatetime = row.getInstant("createDatetime");

            return new ChatGroup(groupIdLong , groupName, groupDescription, null,owner, config, avatar);
        } else {
            // 如果没有找到对应的群组，返回 null 或抛出异常
            return null;
        }
    }


//    @RequestMapping("setRequestCache")
//    public LoginMessage setRequestCache(GroupMessage message) {
//
//    }
}
