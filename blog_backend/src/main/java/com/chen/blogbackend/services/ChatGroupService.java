package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.mappers.GroupParser;
import com.chen.blogbackend.mappers.InvitationMapper;
import com.chen.blogbackend.mappers.MessageParser;
import com.chen.blogbackend.util.RandomUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ChatGroupService {

    private static final Logger logger = LoggerFactory.getLogger(ChatGroupService.class);

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

    PreparedStatement removeGroupMemberByUser;
    PreparedStatement removeGroupMemberByGroup;

    PreparedStatement getGroupMember;
    PreparedStatement getGroupOwner;
    PreparedStatement getGroupMessages;

    PreparedStatement insertInvitation;
    PreparedStatement getInvitation;


    @Autowired
    private CqlSession cqlSession;

    @PostConstruct
    public void init() {
        logger.info("Initializing ChatGroupService prepared statements");

        try {
            //insertChatRecordById = session.prepare("insert into group_chat.group_chat_record_by_id (group_id , message_id ,type ,  user_id  ,content , referUserID , referMessageId , send_time, media, recall) values(?,?,?,?,?,?,?,?,?,?)");
            getGroupDetails = session.prepare("select * from group_chat.chat_group_details where group_id = ?");
//            removeMember = session.prepare("delete from group_chat.chat_group_members where group_id = ? and user_id = ?");
            createChatGroup = session.prepare("insert into group_chat.chat_group_details (group_id, avatar, config, introduction, name, owner_id,create_time,allow_invite_by_token) values(?, ?, ?, ?, ?, ?, ?, ?)");
            getGroups = session.prepare("select * from group_chat.chat_group_members_by_user where user_id = ?");
            getMembers = session.prepare("select * from group_chat.chat_group_members_by_group where group_id = ? ");
            //getRecord = session.prepare("select * from group_chat.group_chat_record_by_id where group_id = ? and message_id = ?");
            //recall = session.prepare("delete from group_chat.group_chat_record_by_id where group_id = ? and message_id = ?");
            getRecordByGroupId = session.prepare("select * from chat.group_chat_records where group_id= ? and session_message_id > ? limit 10");
            //getRecordByMemberId = session.prepare("select * from chat.chat_messages_mailbox where user_id= ? ");
            insertGroupMemberByUser = session.prepare("insert into group_chat.chat_group_members_by_user (group_id, user_id, group_name) values (?, ?, ?)");
            insertGroupMemberByGroup = session.prepare("insert into group_chat.chat_group_members_by_group (group_id, user_id, user_name) values (?, ?, ?)");
            getGroupMember = session.prepare("select * from group_chat.chat_group_members_by_group where group_id = ? and user_id = ?");
            getGroupOwner = session.prepare("select owner_id from group_chat.chat_group_details where group_id = ?;");
            updateGroupDetails = session.prepare("UPDATE group_chat.chat_group_details SET introduction = ?, name = ?, allow_invite_by_token = ? WHERE group_id = ?;");
            insertInvitation = session.prepare("insert into group_chat.invitations (groupId, expire_time, code, userId, create_time) values (?, ?, ?, ?, ?)");
            getInvitation = session.prepare("select * from group_chat.invitations where code = ?;");
            removeGroupMemberByGroup = session.prepare("delete from group_chat.chat_group_members_by_group where user_id = ? and group_id = ?;");
            removeGroupMemberByUser =session.prepare("delete from group_chat.chat_group_members_by_user where user_id = ? and group_id = ?;");
            getGroupMessages = session.prepare("select * from chat.group_chat_records where group_id = ? and session_message_id <= ? order by session_message_id desc limit 15;");


            logger.info("ChatGroupService prepared statements initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize ChatGroupService prepared statements", e);
            throw e;
        }
    }

    public boolean joinGroup(String userId, Long groupId) {
        return joinGroup(userId, groupId, "");
    }

    public boolean joinGroup(String userId, Long groupId, String groupName) {
        logger.debug("User {} attempting to join group {} with name '{}'", userId, groupId, groupName);

        try {
            BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
            builder.addStatements(insertGroupMemberByGroup.bind(groupId, userId, ""));
            builder.addStatements(insertGroupMemberByUser.bind(groupId, userId, groupName));
            ResultSet execute = session.execute(builder.build());

            boolean success = execute.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("User {} successfully joined group {}", userId, groupId);
            } else {
                logger.warn("User {} failed to join group {}: {}", userId, groupId, execute.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error occurred while user {} was joining group {}", userId, groupId, e);
            return false;
        }
    }

    public boolean quitGroup(String userId, String groupId) {
        logger.debug("User {} attempting to quit group {}", userId, groupId);

        try {
            BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
            builder.addStatements(removeGroupMemberByUser.bind(groupId, userId));
            builder.addStatement(removeGroupMemberByGroup.bind(groupId, userId));
            ResultSet execute = session.execute(builder.build());

            boolean success = execute.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("User {} successfully quit group {}", userId, groupId);
            } else {
                logger.warn("User {} failed to quit group {}: {}", userId, groupId, execute.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error occurred while user {} was quitting group {}", userId, groupId, e);
            return false;
        }
    }

    public Invitation generateInvitation( String userId, String groupId) {
        logger.debug("generating invitation for user {} to group {}", userId, groupId);

        try {
            String code = userId + System.currentTimeMillis() + RandomUtil.generateRandomString(10);
            Instant now = Instant.now();
            Instant expire = now.plus(3600 * 24 * 7, ChronoUnit.SECONDS);
            Invitation invitation = new Invitation();
            //groupId, expire_time, code, userId, create_time
            session.execute(insertInvitation.bind(groupId, expire, code, userId, now));
            logger.info("Invitation {} generated successfully for user {} to group {} ",
                    code, userId, groupId);
            return invitation;
        } catch (Exception e) {
            logger.error("Failed to generate invitation for user {} to group {} ", userId, groupId, e);
            throw e;
        }
    }

    public boolean dismissGroup(String operatorId, String groupId) {
        logger.debug("Operator {} attempting to dismiss group {}", operatorId, groupId);

        try {
            BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
            ResultSet execute = session.execute(getMembersId.bind(groupId));

            int memberCount = 0;
            for (Row row : execute.all()) {
                String userId = row.get("user_id", String.class);
                builder.addStatement(removeMember.bind(groupId, userId));
                memberCount++;
            }

            builder.addStatement(delAllChatGroupById.bind(groupId));
            ResultSet execute1 = session.execute(builder.build());

            boolean success = execute1.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("Group {} successfully dismissed by operator {}, removed {} members",
                        groupId, operatorId, memberCount);
            } else {
                logger.warn("Failed to dismiss group {} by operator {}: {}",
                        groupId, operatorId, execute1.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error occurred while operator {} was dismissing group {}", operatorId, groupId, e);
            return false;
        }
    }

    public boolean joinByInvitation(String userId, String username, Long groupId, String invitationID) {
        logger.debug("User {} attempting to join group {} using invitation {}", userId, groupId, invitationID);

        try {
            ResultSet execute = cqlSession.execute(getInvitation.bind(invitationID));
            List<Row> all = execute.all();

            List<Invitation> invitations = InvitationMapper.parseInvitation(execute);
            if ( invitations.size() == 0) {
                logger.warn("Invitation {} not found for user {} joining group {}", invitationID, userId, groupId);
                return false;
            }
            Invitation invitation = invitations.get(0);


            boolean isNotExpired = Instant.now().isBefore(invitation.getExpireTime());

            if (!isNotExpired) {
                logger.warn("Expired invitation {} for user {} joining group {}", invitationID, userId, groupId);
                return false;
            }
            if (invitation.getType().equals("group") && Objects.equals(invitation.getGroupId(), groupId)) {
                BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
                builder.addStatements(insertGroupMemberByUser.bind(groupId, userId, username));
                logger.info("User {} successfully joined group {} using invitation {}", userId, groupId, invitationID);
            } else {
                logger.warn("Expired invitation {} for user {} joining group {}", invitationID, userId, groupId);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error occurred while user {} was joining group {} with invitation {}",
                    userId, groupId, invitationID, e);
            return false;
        }
    }

    public List<GroupUser> getMembers(long groupId) {
        logger.debug("Retrieving members for group {}", groupId);

        try {
            ResultSet execute = session.execute(getMembers.bind(groupId));
            List<GroupUser> members = GroupParser.groupListParser(execute);
            logger.debug("Retrieved {} members for group {}", members.size(), groupId);
            return members;
        } catch (Exception e) {
            logger.error("Error occurred while retrieving members for group {}", groupId, e);
            return new ArrayList<>();
        }
    }

    public List<SingleMessage> getGroupMessageByGroupIDAndTimestamp(Long groupId, long timestamp) {
        logger.debug("Retrieving messages for group {} after timestamp {}", groupId, timestamp);

        try {
            ResultSet execute = session.execute(getRecordByGroupId.bind(groupId, Instant.ofEpochMilli(timestamp)));
            List<SingleMessage> messages = MessageParser.parseToNotificationMessage(execute);
            logger.debug("Retrieved {} messages for group {} after timestamp {}", messages.size(), groupId, timestamp);
            return messages;
        } catch (Exception e) {
            logger.error("Error occurred while retrieving messages for group {} after timestamp {}", groupId, timestamp, e);
            return new ArrayList<>();
        }
    }

    public boolean removeUser(String operatorId, String groupId, String userId) {
        logger.debug("Operator {} attempting to remove user {} from group {}", operatorId, userId, groupId);

        try {
            BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
            builder.addStatements(removeMember.bind(userId, groupId));
            ResultSet execute = session.execute(builder.build());

            boolean success = execute.getExecutionInfo().getErrors().size() == 0;
            if (success) {
                logger.info("User {} successfully removed from group {} by operator {}", userId, groupId, operatorId);
            } else {
                logger.warn("Failed to remove user {} from group {} by operator {}: {}",
                        userId, groupId, operatorId, execute.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error occurred while operator {} was removing user {} from group {}",
                    operatorId, userId, groupId, e);
            return false;
        }
    }

    public List<GroupUser> getGroups(String userId) {
        logger.debug("Retrieving groups for user {}", userId);

        try {
            ResultSet execute = session.execute(getGroups.bind(userId));
            List<GroupUser> groups = GroupParser.groupListParser(execute);
            logger.debug("Retrieved {} groups for user {}", groups.size(), userId);
            return groups;
        } catch (Exception e) {
            logger.error("Error occurred while retrieving groups for user {}", userId, e);
            return new ArrayList<>();
        }
    }

    public boolean recall(String operatorId, long groupID, String messageId) {
        logger.debug("Operator {} attempting to recall message {} from group {}", operatorId, messageId, groupID);
        // TODO: Implement actual recall logic
        logger.warn("Recall functionality not implemented yet for message {} in group {} by operator {}",
                messageId, groupID, operatorId);
        return true;
    }

    public boolean createGroup(String ownerId, String groupName, List<String> members, Boolean allowInvitesByToken) {
        logger.debug("Owner {} creating group '{}' with {} members, allowInvitesByToken: {}",
                ownerId, groupName, members.size(), allowInvitesByToken);

        try {
            long groupId = keyService.getIntKey("chatGroup");
            logger.debug("Generated group ID {} for new group '{}'", groupId, groupName);

            boolean result = true;
            members.add(ownerId);

            for (String memberId : members) {
                result = joinGroup(memberId, groupId, groupName);
                if (!result) {
                    logger.error("Failed to add member {} to group {}, rolling back group creation", memberId, groupId);
                    return false;
                }
            }
            Instant creationTime = Instant.now();
            ResultSet execute = session.execute(createChatGroup.bind(
                    groupId, "", new HashMap<>(), "", groupName, ownerId, creationTime, allowInvitesByToken));
            if (!execute.getExecutionInfo().getErrors().isEmpty()) {
                logger.error("Failed to create group details for group {} '{}': {}",
                        groupId, groupName, execute.getExecutionInfo().getErrors());
                return false;
            }

            logger.info("Group {} '{}' successfully created by owner {} with {} members",
                    groupId, groupName, ownerId, members.size());

            Long messageId = keyService.getLongKey("chat_global");
            Long sessionMessageId = keyService.getLongKey("group_chat_"+ groupId);
            //send message to message queue;
            //String userId, long groupId, long messageId, String content, String messageType,
            //                        Instant sendTime, String type, long referMessageId, List<String> referUserId,
            //                        boolean del, long sessionMessageId)
            //            GroupMessage groupMessage = new GroupMessage(userId, groupId, receipt.getMessageId(), content, messageType, Instant.now(), "group", -1l, new ArrayList<>(), false, receipt.getSessionMessageId());
            notificationProducer.sendNotification(new GroupMessage(ownerId, groupId, messageId,
                    ownerId + " invited you to group " + groupName,"status", creationTime,  "group",-1l, null, false, sessionMessageId));


            return true;
        } catch (Exception e) {
            logger.error("Error occurred while creating group '{}' by owner {}", groupName, ownerId, e);
            return false;
        }
    }

    public boolean isInGroup(String userId, long groupId) {
        logger.debug("Checking if user {} is in group {}", userId, groupId);

        try {
            ResultSet execute = session.execute(getGroupMember.bind(groupId, userId));
            if (execute.getExecutionInfo().getErrors().isEmpty()) {
                boolean isInGroup = !execute.all().isEmpty();
                logger.debug("User {} is {} in group {}", userId, isInGroup ? "" : "not", groupId);
                return isInGroup;
            }
            logger.warn("Error checking if user {} is in group {}: {}", userId, groupId, execute.getExecutionInfo().getErrors());
            return false;
        } catch (Exception e) {
            logger.error("Error occurred while checking if user {} is in group {}", userId, groupId, e);
            return false;
        }
    }

    public SendingReceipt sendGroupMessage(String userId, Long groupId, String content, String messageType,
                                           Long previousId,Long previousSessionId) throws Exception {
        logger.debug("User {} sending message to group {}, type: {}", userId, groupId, messageType);

        Instant now = Instant.now();
        SendingReceipt receipt = new SendingReceipt();

        try {

            previousId = previousId == null ? keyService.getLongKey("chat_global") : previousId;
            previousSessionId = previousSessionId == null ? keyService.getLongKey("group_chat_" + groupId) : previousSessionId;
            receipt.setMessageId(previousId);
            receipt.setSessionMessageId(previousSessionId);

            if (isInGroup(userId, groupId)) {
                GroupMessage groupMessage = new GroupMessage(userId, groupId, receipt.getMessageId(), content,
                        messageType, Instant.now(), "group", -1l, new ArrayList<>(), false,
                        receipt.getSessionMessageId());
                receipt.setResult(true);
                receipt.setTimestamp(now.toEpochMilli());
                notificationProducer.sendNotification(groupMessage);
                logger.info("User {} successfully sent message {} to group {}", userId, receipt.getMessageId(), groupId);
            } else {
                logger.warn("User {} attempted to send message to group {} but is not a member", userId, groupId);
                receipt.setResult(false);
            }
            return receipt;
        } catch (Exception e) {
            logger.error("Error occurred while user {} was sending message to group {}", userId, groupId, e);
            receipt.setResult(false);
            throw e;
        }
    }

    public ChatGroup getGroupDetail(long groupId) {
        logger.debug("Retrieving details for group {}", groupId);

        try {
            Select select = QueryBuilder.selectFrom("group_chat", "chat_group_details").all()
                    .whereColumn("group_id").isEqualTo(QueryBuilder.literal(groupId));

            SimpleStatement build = select.build();
            ChatGroup groupDetail = GroupParser.parseDetails(session.execute(build));

            if (groupDetail != null) {
                logger.debug("Successfully retrieved details for group {}", groupId);
            } else {
                logger.warn("No details found for group {}", groupId);
            }
            return groupDetail;
        } catch (Exception e) {
            logger.error("Error occurred while retrieving details for group {}", groupId, e);
            return null;
        }
    }

//    public List<SingleMessage> getNewestMessages(String userId, long timestamp) {
//        logger.debug("Retrieving newest messages for user {} after timestamp {}", userId, timestamp);
//
//        try {
//            ResultSet execute = session.execute(getGroups.bind(userId));
//            List<SingleMessage> singleMessages = new ArrayList<>();
//
//            if (!execute.getExecutionInfo().getErrors().isEmpty()) {
//                logger.warn("Error retrieving groups for user {}: {}", userId, execute.getExecutionInfo().getErrors());
//                return singleMessages;
//            }
//
//            List<Row> all = execute.all();
//            int totalMessages = 0;
//
//            for (Row row : all) {
//                Long groupId = row.getLong("group_id");
//                List<SingleMessage> groupMessages = getGroupMessageByGroupIDAndTimestamp(groupId, timestamp);
//                singleMessages.addAll(groupMessages);
//                totalMessages += groupMessages.size();
//            }
//
//            logger.debug("Retrieved {} newest messages across {} groups for user {}",
//                    totalMessages, all.size(), userId);
//            return singleMessages;
//        } catch (Exception e) {
//            logger.error("Error occurred while retrieving newest messages for user {} after timestamp {}",
//                    userId, timestamp, e);
//            return new ArrayList<>();
//        }
//    }

    public List<GroupMessage> getGroupMessageRecords(Long groupId, Long lastSessionMessageId) {
        logger.debug("Retrieving message records for group {} after session message ID {}", groupId, lastSessionMessageId);
        ResultSet execute = session.execute(getGroupMessages.bind(groupId, lastSessionMessageId));
        List<GroupMessage> groupMessages = MessageParser.parseToGroupMessage(execute);


        logger.warn("getGroupMessageRecords method not implemented yet for group {} with lastSessionMessageId {}",
                groupId, lastSessionMessageId);
        return groupMessages;
    }

    public boolean updateGroup(String userEmail, long groupId, String name, String description, Boolean allowInviteByToken) {
        logger.debug("User {} attempting to update group {}: name='{}', description='{}', allowInviteByToken={}",
                userEmail, groupId, name, description, allowInviteByToken);

        try {
            ResultSet execute = session.execute(getGroups.bind(groupId));
            Row one = execute.one();

            if (one == null) {
                logger.warn("Group {} not found for update by user {}", groupId, userEmail);
                return false;
            } else {
                String ownerId = one.getString("ownerId");
                if (!ownerId.equals(userEmail)) {
                    logger.warn("User {} is not the owner of group {} (owner: {}), update denied",
                            userEmail, groupId, ownerId);
                    return false;
                }
            }

            session.execute(updateGroupDetails.bind(description, name, allowInviteByToken, groupId));
            logger.info("Group {} successfully updated by owner {}: name='{}', description='{}', allowInviteByToken={}",
                    groupId, userEmail, name, description, allowInviteByToken);
            return true;
        } catch (Exception e) {
            logger.error("Error occurred while user {} was updating group {}", userEmail, groupId, e);
            return false;
        }
    }
}