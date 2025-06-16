package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.SingleMessage;
import com.chen.blogbackend.entities.GroupMessage;
import com.chen.blogbackend.entities.OnlineGroupMessage;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MessageParser {


    public static List<SingleMessage> parseToNotificationMessage(ResultSet set) {
        List<SingleMessage> result = new ArrayList<>();

        for (Row row : set.all()) {
            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();

            // 获取字段（如果不存在则设默认值）
            String userId1 = columnDefinitions.contains("user_id1") ? row.getString("user_id1") : null;
            String userId2 = columnDefinitions.contains("user_id2") ? row.getString("user_id2") : null;
            Long messageId = columnDefinitions.contains("message_id") ? row.getLong("message_id") : 0L;
            String content = columnDefinitions.contains("content") ? row.getString("content") : null;
            Instant sendTime = columnDefinitions.contains("send_time") ? row.getInstant("send_time") : null;
//            String type = columnDefinitions.contains("type") ? row.getString("type") : "single";
            String messageType = columnDefinitions.contains("messagetype") ? row.getString("messagetype") : null;
            Long referMessageId = columnDefinitions.contains("refer_message_id") ? row.getLong("refer_message_id") : 0L;
            boolean direction = columnDefinitions.contains("direction") ? row.getBoolean("direction") : false;
            boolean deleted = columnDefinitions.contains("del") ? row.getBoolean("del") : false;
            Long sessionMessageId = columnDefinitions.contains("session_message_id") ? row.getLong("session_message_id") : 0L;

            // 创建 SingleMessage 对象（avatar、receiverName、senderName 可留空或后续补齐）
            SingleMessage message = new SingleMessage(
                    "",               // avatar
                    userId1,
                    userId2,
                    "",               // receiverName
                    "",               // senderName
                    "single",
                    content,
                    sendTime,
                    messageId,
                    referMessageId,
                    messageType,
                    direction,
                    deleted,
                    sessionMessageId
            );

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
        List<GroupMessage> result = new ArrayList<>();
        for (Row row : set.all()) {
            GroupMessage message = new GroupMessage();
            message.setUserId(row.getString("user_id"));
            message.setGroupId(row.getLong("group_id"));
            message.setMessageId(row.getLong("message_id"));
            message.setContent(row.getString("content"));
            message.setMessageType(row.getString("messagetype"));
            message.setTimestamp(row.getInstant("send_time"));
            message.setType(row.getString("type"));
            message.setReferMessageId(row.getLong("refer_message_id"));
            message.setReferUserId(row.getList("refer_user_id", String.class));
            message.setDel(row.getBoolean("del"));
            message.setSessionMessageId(row.getLong("session_message_id"));
            result.add(message);
        }
        System.out.println(result.size());
        return result;
    }

}
