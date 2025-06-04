package com.chen.notification.mappers;


import com.chen.notification.entities.NotificationMessage;
import com.chen.notification.entities.OnlineGroupMessage;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MessageParser {


    public static List<NotificationMessage> parseToNotificationMessage(ResultSet set) {
        ArrayList<NotificationMessage> result = new ArrayList<>();
        for (Row row : set.all()) { // 假设 resultSet 是某种支持 all() 的类型

            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();

// 检查并获取每列的值
            String senderId = columnDefinitions.contains("user_id") ? row.getString("user_id") : null;
            String receiverId = columnDefinitions.contains("receiver_id") ? row.getString("receiver_id") : null;
            Long messageId = columnDefinitions.contains("message_id") ? row.getLong("message_id") : null;
            String content = columnDefinitions.contains("content") ? row.getString("content") : null;
            Instant sendTime = columnDefinitions.contains("send_time") ? row.getInstant("send_time") : null;
            String type = columnDefinitions.contains("type") ? row.getString("type") : null;
            String messageType = columnDefinitions.contains("messageType") ? row.getString("messageType") : null;
            Long referMessageId = columnDefinitions.contains("refer_message_id") ? row.getLong("refer_message_id") : null;
            Long groupId = columnDefinitions.contains("group_id") ? row.getLong("group_id") : null;
            System.out.println(senderId);
// 创建 NotificationMessage 对象
            NotificationMessage message = new NotificationMessage(
                    "", // Assuming avatar is not provided by the database, set to null or default
                    senderId,
                    receiverId,
                    groupId != null ? groupId : 0L, // 如果 groupId 为空，设置为默认值 0L
                    "", // receiverName can be set to null or fetched if available
                    "", // senderName can be set to null or fetched if available
                    type,
                    content,
                    messageId != null ? messageId : 0L, // 如果 messageId 为空，设置为默认值 0L
                    sendTime,
                    0L // 这里是固定值
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

}
