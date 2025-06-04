package com.chen.notification.mappers;

import com.chen.blogbackend.entities.UnreadMessage;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UnreadMessageParser {

    public static List<UnreadMessage> parseToUnreadMessage(ResultSet set) {
        ArrayList<UnreadMessage> result = new ArrayList<>();
        for (Row row : set.all()) {
            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();

            // 检查并获取每列的值
            String userId = columnDefinitions.contains("user_id") ? row.getString("user_id") : null;
            String receiverId = columnDefinitions.contains("receiver_id") ? row.getString("receiver_id") : null;
            String type = columnDefinitions.contains("type") ? row.getString("type") : null;
            String messageType = columnDefinitions.contains("messageType") ? row.getString("messageType") : null;
            String content = columnDefinitions.contains("content") ? row.getString("content") : null;
            Instant sendTime = columnDefinitions.contains("send_time") ? row.getInstant("send_time") : null;
            int messageId = columnDefinitions.contains("message_id") ? row.getInt("message_id") : 0;

            // 创建 UnreadMessage 对象
            UnreadMessage message = new UnreadMessage(
                    userId,
                    receiverId,
                    type,
                    messageType,
                    content,
                    sendTime,
                    messageId
            );

            // 将解析的消息添加到结果列表中
            result.add(message);
        }
        return result;
    }
}
