package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.NotificationMessage;
import com.chen.blogbackend.entities.deprecated.GroupMessage;
import com.chen.blogbackend.entities.OnlineGroupMessage;
import com.chen.blogbackend.entities.deprecated.SingleMessage;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MessageParser {


    public static List<NotificationMessage> parseToNotificationMessage(ResultSet set) {
        ArrayList<NotificationMessage> result = new ArrayList<>();
        for (Row row : set.all()) { // 假设 resultSet 是某种支持 all() 的类型
            String senderId = row.getString("user_id");
            String receiverId = row.getString("receiver_id");
            long messageId = row.getLong("message_id");
            String content = row.getString("content");
            Instant sendTime = row.getInstant("send_time");  // Convert timestamp to Instant
            String type = row.getString("type");
            String messageType = row.getString("messageType");
            String referMessageId = row.getString("refer_message_id");
            long groupId = row.getLong("group_id");
            // 创建 NotificationMessage 对象
            NotificationMessage message = new NotificationMessage(
                    "",  // Assuming avatar is not provided by the database, set to null or default
                    senderId,
                    receiverId,
                    groupId,
                    "",  // receiverName can be set to null or fetched if available
                    "",  // senderName can be set to null or fetched if available
                    type,
                    content,
                    messageId,
                    sendTime,
                    0l
            );

            // 将解析的消息添加到结果列表中
            result.add(message);
        }
        return result;
    }

    public static List<OnlineGroupMessage> parseToOnlineGroupMessage(ResultSet set) {
        ArrayList<OnlineGroupMessage> result = new ArrayList<>();
        for (Row row : set.all()) {
            result.add(new OnlineGroupMessage(
                    row.getLong("user_id"),                // 将bigint映射为Java的Long类型
                    row.getLong("group_id"),               // group_id 也是 bigint
                    row.getString("type"),                 // type 是 text 类型
                    row.getString("content"),              // content 是 text 类型
                    row.getInt("count"),               // count 是 text 类型
                    row.getInstant("latest_timestamp")
            ));
        }
        return result;

    }

    public static List<GroupMessage> parseToGroupMessage(ResultSet set) {
        ArrayList<GroupMessage> result = new ArrayList<>();
        for (Row row : set.all()) {
            result.add(new GroupMessage(
                    row.getBool("recall"),                 // recall 是 boolean 类型
                    row.getLong("referMessageId"),         // referMessageId 是 bigint
                    row.getLong("referUserID"),            // referUserID 是 bigint
                    row.getString("media"),                // media 是 text
                    row.getString("content"),              // content 是 text
                    row.getInstant("send_time"),         // send_time 是 timestamp
                    row.getString("type"),                 // type 是 text
                    row.getLong("group_id"),               // group_id 是 bigint
                    row.getLong("user_id"),                // user_id 是 bigint
                    row.getLong("message_id")              // message_id 是 bigint
            ));
        }
        System.out.println(result.size());
        return result;
    }
}
