package com.chen.blogbackend.services;

import com.alibaba.fastjson2.JSON;
import com.chen.blogbackend.entities.SingleMessage;
import com.chen.blogbackend.entities.SendingReceipt;
import com.chen.blogbackend.entities.UnreadMessage;
import com.chen.blogbackend.mappers.MessageParser;
import com.chen.blogbackend.mappers.UnreadMessageParser;
import com.datastax.oss.driver.api.core.CqlSession;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SingleMessageService {

    private static final Logger logger = LoggerFactory.getLogger(SingleMessageService.class);

    @Autowired
    CqlSession session;

    @Autowired
    NotificationProducer producer;

    @Autowired
    FriendsService friendsService;

    @Autowired
    KeyService keyService;

    @Autowired
    JedisPool pool;

    PreparedStatement getSingleRecords;
    PreparedStatement block;
    PreparedStatement unBlock;
    PreparedStatement addEndpoint;
    PreparedStatement getEndpoints;
    PreparedStatement getGroupRecord;
    PreparedStatement getNewestMessageFromAllUsers;
    PreparedStatement deleteUnread;
    PreparedStatement insertSingleMessage;
    PreparedStatement deleteGroupUnread;

    @Autowired
    private ChatGroupService chatGroupService;

    @Autowired
    private CqlSession cqlSession;

    @PostConstruct
    public void init(){
        logger.info("Initializing SingleMessageService and preparing CQL statements");

        try {
            block = session.prepare("insert into userinfo.black_list (user_id, black_user_id, black_user_name, black_user_avatar) values(?, ?, ?, ?)");
            unBlock = session.prepare("delete from userInfo.black_list where user_id = ? and black_user_id = ?");
            addEndpoint = session.prepare("INSERT INTO chat.web_push_endpoints (user_id, endpoint, expiration_time, p256dh, auth) VALUES (?, ?, ?, ?, ?);");
            getEndpoints = session.prepare("select * from chat.web_push_endpoints where user_id = ?");
            getSingleRecords = session.prepare("select * from chat.chat_records where user_id1 = ? and user_id2 = ? and session_message_id <= ? order by session_message_id desc limit 15");
            getNewestMessageFromAllUsers = session.prepare("select * from chat.unread_messages where user_id = ?;");
            deleteUnread = session.prepare("delete from chat.unread_messages where user_id = ? and type =? and sender_id =? and group_id = ?;");
            deleteGroupUnread = session.prepare("delete from chat.unread_messages where user_id = ? and type = ? and group_id = ?");
            insertSingleMessage = session.prepare("INSERT INTO chat.chat_records (user_id1, user_id2, direction, " +
                    "relationship, group_id, message_id, content, messagetype, send_time, refer_message_id," +
                    " refer_user_id, del, session_message_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

            logger.info("Successfully initialized all prepared statements");
        } catch (Exception e) {
            logger.error("Failed to initialize prepared statements", e);
            throw new RuntimeException("Failed to initialize SingleMessageService", e);
        }
    }

    public boolean blockUser(String userId, String blockUser) {
        logger.info("Attempting to block user - userId: {}, blockUser: {}", userId, blockUser);

        try {
            ResultSet execute = session.execute(block.bind(userId, blockUser));
            boolean success = execute.getExecutionInfo().getErrors().isEmpty();

            if (success) {
                logger.info("Successfully blocked user - userId: {}, blockUser: {}", userId, blockUser);
            } else {
                logger.warn("Failed to block user - userId: {}, blockUser: {}, errors: {}",
                        userId, blockUser, execute.getExecutionInfo().getErrors());
            }

            return success;
        } catch (Exception e) {
            logger.error("Exception occurred while blocking user - userId: {}, blockUser: {}", userId, blockUser, e);
            return false;
        }
    }

    public boolean unblockUser(String userId, String unBlockUser) {
        logger.info("Attempting to unblock user - userId: {}, unBlockUser: {}", userId, unBlockUser);

        try {
            ResultSet execute = session.execute(unBlock.bind(userId, unBlockUser));
            boolean success = execute.getExecutionInfo().getErrors().isEmpty();

            if (success) {
                logger.info("Successfully unblocked user - userId: {}, unBlockUser: {}", userId, unBlockUser);
            } else {
                logger.warn("Failed to unblock user - userId: {}, unBlockUser: {}, errors: {}",
                        userId, unBlockUser, execute.getExecutionInfo().getErrors());
            }

            return success;
        } catch (Exception e) {
            logger.error("Exception occurred while unblocking user - userId: {}, unBlockUser: {}", userId, unBlockUser, e);
            return false;
        }
    }

    public SendingReceipt sendMessage(String userId, String receiverId, String content, String type,String messageType) throws Exception {
        logger.info("Attempting to send message - userId: {}, receiverId: {}, type: {}", userId, receiverId, type);

        SendingReceipt receipt = new SendingReceipt();

        try {
            if (friendsService.getRelationship(userId, receiverId) != 11) {
                logger.warn("Users are not friends - cannot send message. userId: {}, receiverId: {}", userId, receiverId);
                receipt.setMessageId(-1);
                receipt.setResult(false);
                return receipt;
            }

            Instant now = Instant.now();
            String[] users = sortUsers(userId, receiverId);
            Boolean direction = false;
            if (users[0].equals(userId)) {
                direction = true;
            }
            userId = users[0];
            receiverId = users[1];

            receipt.setMessageId(keyService.getLongKey("chat_global"));
            receipt.setSessionMessageId(keyService.getLongKey("chat_" + users[0] + "_" + users[1]));
            receipt.setDelete(false);

            logger.debug("Generated message IDs - messageId: {}, sessionMessageId: {}",
                    receipt.getMessageId(), receipt.getSessionMessageId());

            SingleMessage singleMessage = new SingleMessage("", userId, receiverId, "", "",
                    "single", content, now, receipt.getMessageId(), -1, messageType,
                    direction, false, receipt.getSessionMessageId());

            BoundStatement bound = insertSingleMessage.bind(
                    singleMessage.getUserId1(),
                    singleMessage.getUserId2(),
                    direction,
                    false,
                    0L,
                    singleMessage.getMessageId(),
                    singleMessage.getContent(),
                    singleMessage.getMessageType(),
                    singleMessage.getTimestamp(),
                    -1L,
                    Collections.emptyList(),
                    singleMessage.isDeleted(),
                    singleMessage.getSessionMessageId()
            );

            cqlSession.execute(bound);
            logger.debug("Successfully inserted message into database");

            producer.sendNotification(singleMessage);
            logger.debug("Successfully sent notification");

            receipt.setResult(true);
            receipt.setTimestamp(now.toEpochMilli());

            logger.info("Successfully sent message - messageId: {}, sessionMessageId: {}",
                    receipt.getMessageId(), receipt.getSessionMessageId());

            return receipt;
        } catch (Exception e) {
            logger.error("Failed to send message - userId: {}, receiverId: {}, messageType: {}",
                    userId, receiverId, messageType, e);
            receipt.setResult(false);
            throw e;
        }
    }

    public static String[] sortUsers(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) {
            throw new IllegalArgumentException("User IDs cannot be null");
        }

        String[] result = new String[2];
        if (userId1.compareTo(userId2) < 0) {
            result[0] = userId1;
            result[1] = userId2;
        } else {
            result[0] = userId2;
            result[1] = userId1;
        }
        return result;
    }

    public List<SingleMessage> getSingleMessageRecords(String userId, String anotherUserId, Long sessionMessageId) {
        logger.info("Retrieving single message records - userId: {}, anotherUserId: {}, sessionMessageId: {}",
                userId, anotherUserId, sessionMessageId);

        try {
            String[] strings = sortUsers(userId, anotherUserId);
            userId = strings[0];
            anotherUserId = strings[1];

            ResultSet execute = session.execute(getSingleRecords.bind(userId, anotherUserId, sessionMessageId));
            List<SingleMessage> singleMessages = MessageParser.parseToNotificationMessage(execute);

            logger.info("Successfully retrieved {} message records for users: {} and {}",
                    singleMessages.size(), userId, anotherUserId);

            return singleMessages;
        } catch (Exception e) {
            logger.error("Failed to retrieve message records - userId: {}, anotherUserId: {}, sessionMessageId: {}",
                    userId, anotherUserId, sessionMessageId, e);
            return new ArrayList<>();
        }
    }

    public List<SingleMessage> getUnreadCount(String userId) {
        logger.info("Retrieving unread messages from Redis for userId: {}", userId);

        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Map<String, String> redisHash = jedis.hgetAll(userId);

            if (redisHash.isEmpty()) {
                logger.info("No unread messages found in Redis for userId: {}", userId);
                return new ArrayList<>();
            }

            List<SingleMessage> deserializedList = redisHash.values().stream()
                    .map(value -> {
                        try {
                            return JSON.parseObject(value, SingleMessage.class);
                        } catch (Exception e) {
                            logger.error("Error deserializing message value: {}", value, e);
                            throw new RuntimeException("Error deserializing value: " + value, e);
                        }
                    })
                    .collect(Collectors.toList());

            logger.info("Successfully retrieved {} unread messages for userId: {}", deserializedList.size(), userId);
            return deserializedList;
        } catch (Exception e) {
            logger.error("Failed to retrieve unread messages from Redis for userId: {}", userId, e);
            return new ArrayList<>();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public boolean addOrUpdateEndpoint(String userId, String endpoint, String p256dh, String auth) {
        logger.info("Adding/updating web push endpoint for userId: {}", userId);

        try {
            ResultSet execute = session.execute(addEndpoint.bind(userId, endpoint, Instant.now(), p256dh, auth));

            if (!execute.getExecutionInfo().getErrors().isEmpty()) {
                logger.error("Errors occurred while adding endpoint for userId: {}, errors: {}",
                        userId, execute.getExecutionInfo().getErrors());
                execute.getExecutionInfo().getErrors().forEach(error -> logger.error("CQL Error: {}", error));
                return false;
            }

            logger.info("Successfully added/updated endpoint for userId: {}", userId);
            return true;
        } catch (Exception e) {
            logger.error("Exception occurred while adding/updating endpoint for userId: {}", userId, e);
            return false;
        }
    }

    public List<String> getEndpoints(String userId) {
        logger.info("Retrieving endpoints for userId: {}", userId);

        try {
            LinkedList<String> strings = new LinkedList<>();
            ResultSet execute = session.execute(getEndpoints.bind(userId));

            for (Row row : execute.all()) {
                List<String> endpoints = row.getList("endpoints", String.class);
                if (endpoints != null) {
                    strings.addAll(endpoints);
                }
            }

            logger.info("Retrieved {} endpoints for userId: {}", strings.size(), userId);
            return strings;
        } catch (Exception e) {
            logger.error("Failed to retrieve endpoints for userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    public List<UnreadMessage> getNewestMessagesFromAllUsers(String userId) {
        logger.info("Retrieving newest messages from all users for userId: {}", userId);

        try {
            ResultSet execute = session.execute(getNewestMessageFromAllUsers.bind(userId));
            List<UnreadMessage> notificationMessages = UnreadMessageParser.parseToUnreadMessage(execute);

            logger.info("Successfully retrieved {} unread messages for userId: {}", notificationMessages.size(), userId);
            return notificationMessages;
        } catch (Exception e) {
            logger.error("Failed to retrieve newest messages for userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    public boolean markUnread(String userId, String senderId, String type, Long groupId) {
        logger.info("Marking message as read - userId: {}, senderId: {}, type: {}, groupId: {}",
                userId, senderId, type, groupId);

        try {

            boolean success;
            ResultSet execute;
            if (type.equals("group")) {
                execute = session.execute(deleteGroupUnread.bind(userId, type, groupId));

            } else {
                execute = session.execute(deleteUnread.bind(userId, type, senderId, groupId));
            }
            success = execute.getExecutionInfo().getErrors().isEmpty();

            if (success) {
                logger.info("Successfully marked message as read - userId: {}, senderId: {}, type: {}, groupId: {}",
                        userId, senderId, type, groupId);
            } else {
                logger.warn("Failed to mark message as read - userId: {}, senderId: {}, type: {}, groupId: {}, errors: {}",
                        userId, senderId, type, groupId, execute.getExecutionInfo().getErrors());
            }

            return success;
        } catch (Exception e) {
            logger.error("Exception occurred while marking message as read - userId: {}, senderId: {}, type: {}, groupId: {}",
                    userId, senderId, type, groupId, e);
            return false;
        }
    }
}