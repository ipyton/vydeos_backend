package com.chen.blogbackend.services;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson2.JSON;
import com.chen.blogbackend.DAO.SingleMessageDao;
import com.chen.blogbackend.entities.NotificationMessage;
import com.chen.blogbackend.entities.NotificationSubscription;
import com.chen.blogbackend.entities.SendingReceipt;
import com.chen.blogbackend.entities.deprecated.SingleMessage;
import com.chen.blogbackend.mappers.MessageParser;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.datastax.oss.driver.api.core.CqlSession;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PagingState;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.metadata.Node;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.nio.charset.StandardCharsets;
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


    //PreparedStatement getRecord; //Caused by: com.datastax.oss.driver.api.core.servererrors.InvalidQueryException: Only EQ and IN relation are supported on the partition key (unless you use the token() function or allow filtering)
    PreparedStatement setRecordById;
    PreparedStatement getAllRecords;
    PreparedStatement block;
    PreparedStatement unBlock;
//    PreparedStatement recall;
//    PreparedStatement getNewestRecord;
    //PreparedStatement updateEndpoint;
    PreparedStatement addEndpoint;
    PreparedStatement getEndpoints;
    PreparedStatement getGroupRecord;
    PreparedStatement getNewestMessageFromAllUsers;


    @Autowired
    private ChatGroupService chatGroupService;

//    PreparedStatement updateEndpoint;


    @PostConstruct
    public void init(){
        setRecordById = session.prepare("insert into chat.chat_records (user_id, receiver_id, message_id, content, " +
                "send_time, type, refer_message_id, refer_user_id ) values (?,?,?,?,?,?,?,?);");
        //getRecord = session.prepare("select * from chat.chat_records where user_id = ? and  send_time>? and receiver_id = ?");
        block = session.prepare("insert into userinfo.black_list (user_id, black_user_id, black_user_name, black_user_avatar) values(?, ?, ?, ?)");
        unBlock = session.prepare("delete from userInfo.black_list where user_id = ? and black_user_id = ?");
        //recall = session.prepare("update chat.chat_records set del=true where user_id = ? and message_id= ?");
//        getNewestRecord = session.prepare("select * from chat.chat_records where user_id = ? and send_time > ?");
        //updateEndpoint = session.prepare("update chat.web_push_endpoints set endpoints = endpoints + ? where user_id = ?");
        addEndpoint = session.prepare("INSERT INTO chat.web_push_endpoints (user_id, endpoint, expiration_time, p256dh, auth) VALUES (?, ?, ?, ?, ?);");
//        updateEndpoint = session.prepare("UPDATE chat.web_push_endpoints SET endpoint = ?, p256dh = ?, auth = ? WHERE user_id = ?;");
        getEndpoints = session.prepare("select * from chat.web_push_endpoints where user_id = ?");
        getAllRecords = session.prepare("select * from chat.chat_records where receiver_id = ? and send_time>?");
        getNewestMessageFromAllUsers = session.prepare("select * from chat.unread_messages where receiver_id = ? order by send_time asc");
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

        Instant now = Instant.now();
        SendingReceipt receipt = new SendingReceipt();
        NotificationMessage singleMessage =  new NotificationMessage(null, userId, receiverId,0, null, null, "single", content, -1, now,-1);
        System.out.println(singleMessage.getSenderId());
        System.out.println(singleMessage.getReceiverId());
        if (friendsService.getRelationship(singleMessage.getSenderId(), singleMessage.getReceiverId()) != 11) {
            System.out.println("they are not friends");
            receipt.sequenceId = -1;
            receipt.result = false;

            return receipt;
        }
        String result;
        if (userId.compareTo(receiverId) <= 0) {
            result = userId + receiverId;
        } else {
            result = receiverId + userId;
        }

        receipt.sequenceId = keyService.getIntKey(result);
        singleMessage.setMessageId(receipt.sequenceId);
        //(user_id, receiver_id, message_id, content, send_time, type, messageType, count, refer_message_id, refer_user_id )
//        ResultSet execute = session.execute(setRecordById.bind(singleMessage.getSenderId(),
//                singleMessage.getReceiverId(), singleMessage.getMessageId(), singleMessage.getContent(),
//                singleMessage.getTime(), singleMessage.getType(),
//                singleMessage.getReferMessageId(), new ArrayList<>()));
//        if (!execute.getExecutionInfo().getErrors().isEmpty()) {
//            System.out.println(execute.getExecutionInfo().getErrors());
//        }
        //judge if a user can send message
        producer.sendNotification(singleMessage);
        receipt.result = true;
        receipt.timestamp = now.toEpochMilli();

        //    private String userId;
        //    private String title;
        //    private String content;
        //    private String type;
        //    private String time;
        //producer.sendNotification(singleMessage);

        //producer.sendNotification(new Notification());
        return receipt;
    }

//    public boolean recall(String userId, String receiverId, String messageId){
//        ResultSet set = session.execute(recall.bind(userId, receiverId, messageId));
//        return set.getExecutionInfo().getErrors().isEmpty();
//    }


    public List<NotificationMessage> getNewestMessages(String userId, long timestamp, String pageState) {
        if (timestamp == -1) {
            timestamp = 0;
        }
        List<NotificationMessage> newestMessages = chatGroupService.getNewestMessages(userId, timestamp);

        ResultSet execute = session.execute(getAllRecords.bind(userId, Instant.ofEpochMilli(timestamp)));
        //System.out.println(execute.all().size());
        List<NotificationMessage> notificationMessages = MessageParser.parseToNotificationMessage(execute);
        System.out.println(notificationMessages.size());
        notificationMessages.addAll(newestMessages);
        return notificationMessages;
    }


    //点进去的时候再全拉

    public List<NotificationMessage> getUnreadCount(String userId) {
        Jedis jedis = pool.getResource();

        // 获取 Redis 中的 Hash
        Map<String, String> redisHash = jedis.hgetAll(userId);

        // 检查是否获取到数据
        if (redisHash.isEmpty()) {
            System.out.println("Hash is empty or does not exist!");
            return null;
        }


        // 假设值是 JSON 序列化对象
        List<NotificationMessage> deserializedList = redisHash.values().stream()
                .map(value -> {
                    try {
                        return JSON.parseObject(value, NotificationMessage.class);
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


    public List<NotificationMessage> getNewestMessagesFromAllUsers(String userId) {
        ResultSet execute = session.execute(getNewestMessageFromAllUsers.bind(userId));
        List<NotificationMessage> notificationMessages = MessageParser.parseToNotificationMessage(execute);
        return notificationMessages;
    }
}
