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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SingleMessageService {

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


//    PreparedStatement setRecordById;
    PreparedStatement getSingleRecords;
    PreparedStatement block;
    PreparedStatement unBlock;
//    PreparedStatement recall;
//    PreparedStatement getNewestRecord;
    //PreparedStatement updateEndpoint;
    PreparedStatement addEndpoint;
    PreparedStatement getEndpoints;
    PreparedStatement getGroupRecord;
    PreparedStatement getNewestMessageFromAllUsers;
    PreparedStatement deleteUnread;
    PreparedStatement insertSingleMessage;


    @Autowired
    private ChatGroupService chatGroupService;
    @Autowired
    private CqlSession cqlSession;

//    PreparedStatement updateEndpoint;


    @PostConstruct
    public void init(){
//        setRecordById = session.prepare("insert into chat.chat_records (user_id, receiver_id, message_id, content, " +
//                "send_time, type, refer_message_id, refer_user_id ) values (?,?,?,?,?,?,?,?);");
        //getRecord = session.prepare("select * from chat.chat_records where user_id = ? and  send_time>? and receiver_id = ?");
        block = session.prepare("insert into userinfo.black_list (user_id, black_user_id, black_user_name, black_user_avatar) values(?, ?, ?, ?)");
        unBlock = session.prepare("delete from userInfo.black_list where user_id = ? and black_user_id = ?");
        //recall = session.prepare("update chat.chat_records set del=true where user_id = ? and message_id= ?");
//        getNewestRecord = session.prepare("select * from chat.chat_records where user_id = ? and send_time > ?");
        //updateEndpoint = session.prepare("update chat.web_push_endpoints set endpoints = endpoints + ? where user_id = ?");
        addEndpoint = session.prepare("INSERT INTO chat.web_push_endpoints (user_id, endpoint, expiration_time, p256dh, auth) VALUES (?, ?, ?, ?, ?);");
//        updateEndpoint = session.prepare("UPDATE chat.web_push_endpoints SET endpoint = ?, p256dh = ?, auth = ? WHERE user_id = ?;");
        getEndpoints = session.prepare("select * from chat.web_push_endpoints where user_id = ?");
        getSingleRecords = session.prepare("select * from chat.chat_records where user_id1 = ? and user_id2 = ?and session_message_id > ? limit 10");
        getNewestMessageFromAllUsers = session.prepare("select * from chat.unread_messages where user_id = ?;");
        deleteUnread = session.prepare("delete from chat.unread_messages where user_id = ? and type =? and sender_id =? and group_id = ?;");
        insertSingleMessage = session.prepare("INSERT INTO chat.chat_records (user_id1, user_id2, direction, " +
                "relationship, group_id, message_id, content, messagetype, send_time, refer_message_id," +
                " refer_user_id, del, session_message_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

    }

    public boolean blockUser(String userId, String blockUser) {
        ResultSet execute = session.execute(block.bind(userId, blockUser));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean unblockUser(String userId, String unBlockUser) {
        ResultSet execute = session.execute(unBlock.bind(userId, unBlockUser));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }


//    public List<NotificationMessage> getMessageByUserId(String userId, String receiverId, String type, Long groupid, Long timestamp) {
//        if (type.equals("single")) {
//            ResultSet execute = session.execute(getRecord.bind(userId, receiverId,Instant.ofEpochMilli(timestamp)));
//            return MessageParser.parseToNotificationMessage(execute);
//
//        } else {
//            return chatGroupService.getGroupMessageByGroupIDAndTimestamp(groupid, timestamp);
//        }
//    }



    public SendingReceipt sendMessage(String userId, String receiverId, String content, String messageType) throws Exception {
        SendingReceipt receipt = new SendingReceipt();

        if (friendsService.getRelationship(userId, receiverId) != 11) {
            System.out.println("they are not friends");
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
        SingleMessage singleMessage =  new SingleMessage("", userId, receiverId,"", "",
                "single", content, now, receipt.getMessageId(), -1,messageType,
                direction,false, receipt.getSessionMessageId());
        //session.prepare("INSERT INTO chat.chat_records (user_id1, user_id2, direction, " +
        //                "relationship, group_id, message_id, content, messagetype, send_time, refer_message_id," +
        //                " refer_user_id, del, session_message_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

        //create table chat.chat_records(user_id1 text, user_id2 text, direction boolean,relationship boolean,
        // group_id bigint, message_id bigint, content text,messagetype text, send_time timestamp,
        // refer_message_id bigint, refer_user_id list<text>,  del boolean,session_message_id bigint,
        // PRIMARY KEY ((user_id1, user_id2), session_message_id));
        BoundStatement bound = insertSingleMessage.bind(
                singleMessage.getUserId1(),              // user_id1
                singleMessage.getUserId2(),              // user_id2
                direction,             // direction
                false,                                   // relationship（如果没有，先设 false 或根据逻辑设定）
                0L,                                      // group_id（私聊为 0）
                singleMessage.getMessageId(),            // message_id
                singleMessage.getContent(),              // content
                singleMessage.getMessageType(),          // messagetype
                singleMessage.getTime(),                 // send_time (java.time.Instant)
                -1L,       // refer_message_id
                Collections.emptyList(),                 // refer_user_id（无@人可设为空列表）
                singleMessage.isDeleted(),               // del
                singleMessage.getSessionMessageId()      // session_message_id
        );
        cqlSession.execute(bound);
        producer.sendNotification(singleMessage);
        receipt.setResult(true);
        receipt.setTimestamp(now.toEpochMilli());

        return receipt;
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




    public List<SingleMessage> getNewestMessages(String userId, String anotherUserId, Long sessionMessageId) {
        String[] strings = sortUsers(userId, anotherUserId);
        userId = strings[0];
        anotherUserId = strings[1];


        ResultSet execute = session.execute(getSingleRecords.bind(userId, anotherUserId, sessionMessageId));
        //System.out.println(execute.all().size());
        List<SingleMessage> singleMessages = MessageParser.parseToNotificationMessage(execute);
        System.out.println(singleMessages.size());
        return singleMessages;
    }


    //点进去的时候再全拉

    public List<SingleMessage> getUnreadCount(String userId) {
        Jedis jedis = pool.getResource();

        // 获取 Redis 中的 Hash
        Map<String, String> redisHash = jedis.hgetAll(userId);

        // 检查是否获取到数据
        if (redisHash.isEmpty()) {
            System.out.println("Hash is empty or does not exist!");
            return null;
        }


        // 假设值是 JSON 序列化对象
        List<SingleMessage> deserializedList = redisHash.values().stream()
                .map(value -> {
                    try {
                        return JSON.parseObject(value, SingleMessage.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Error deserializing value: " + value, e);
                    }
                })
                .collect(Collectors.toList());
    return deserializedList;
        // 对对象列表进行排序（按某字段排序，如 age）
        //deserializedList.sort(Comparator.comparingInt(MyObject::getAge));

        // 打印排序后的结果


        // 如果需要，可以将排序后的结果存回 Redis
//        Map<String, String> sortedRedisHash = new LinkedHashMap<>();
//        for (MyObject obj : deserializedList) {
//            String serializedValue = objectMapper.writeValueAsString(obj);
//            sortedRedisHash.put(obj.getId(), serializedValue);
//        }
//        jedis.hmset(hashKey, sortedRedisHash);
//
//        System.out.println("Sorted hash saved back to Redis!");
    }


//    public List<NotificationMessage> getNewRecords( long receiverId, Long timestamp, String pagingState) {
//        System.out.println(receiverId);
//        ResultSet execute;
//        if (null == timestamp) {
//            //get the things the user send
//            execute = session.execute(getNewestRecord.bind(receiverId, receiverId).setPagingState(PagingState.fromString(pagingState)).setPageSize(10));
//        }
//        else {
//            execute = session.execute(getNewestRecord.bind(receiverId, receiverId + "_" + timestamp));
//        }
//
//        return MessageParser.parseToNotificationMessage(execute);
//    }

    //目前只支持单用户
    public boolean addOrUpdateEndpoint(String userId, String endpoint, String p256dh, String auth ) {
//        ResultSet resultSet = session.execute(getEndpoints.bind(userId));
        ResultSet execute = null;
        execute = session.execute(addEndpoint.bind(userId, endpoint, Instant.now(), p256dh, auth));

//        if (!resultSet.iterator().hasNext()) {
//            // 如果不存在，插入新行
//            System.out.println("插入");
//            execute = session.execute(addEndpoint.bind(userId, endpoint, Instant.now(), p256dh, auth));
//        } else {
//            // 如果已经存在，更新数据
//            System.out.println("更新");
////            execute = session.execute(updateEndpoint.bind(endpoint, p256dh, auth, userId));
//        }
        execute.getExecutionInfo().getErrors().forEach(System.out::println);

        return execute != null && execute.getExecutionInfo().getErrors().isEmpty();
    }

    public List<String> getEndpoints(String userId){
        LinkedList<String> strings = new LinkedList<>();

        ResultSet execute = session.execute(getEndpoints.bind(userId));
        for (Row row : execute.all()) {
            strings.addAll(Objects.requireNonNull(row.getList("endpoints", String.class)));
        }
        return strings;
    }


    public List<UnreadMessage> getNewestMessagesFromAllUsers(String userId) {
        ResultSet execute = session.execute(getNewestMessageFromAllUsers.bind(userId));
        List<UnreadMessage> notificationMessages = UnreadMessageParser.parseToUnreadMessage(execute);
        return notificationMessages;
    }

    public boolean markUnread(String userId,String senderId, String type, Long groupId) {

        ResultSet execute = session.execute(deleteUnread.bind(userId, type, senderId, groupId));
        return execute.getExecutionInfo().getErrors().isEmpty();

    }
}
