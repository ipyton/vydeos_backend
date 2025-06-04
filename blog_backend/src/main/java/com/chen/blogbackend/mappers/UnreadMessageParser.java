package com.chen.blogbackend.mappers;

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
            String senderId = columnDefinitions.contains("sender_id") ? row.getString("sender_id") : null;
            String type = columnDefinitions.contains("type") ? row.getString("type") : null;
            String messageType = columnDefinitions.contains("messageType") ? row.getString("messageType") : null;
            String content = columnDefinitions.contains("content") ? row.getString("content") : null;
            Instant sendTime = columnDefinitions.contains("send_time") ? row.getInstant("send_time") : null;

            long messageId = columnDefinitions.contains("message_id") ? row.getLong("message_id") : 0;
            int count = columnDefinitions.contains("count") ? row.getInt("count") : 0;
            String memberId = columnDefinitions.contains("member_id") ? row.getString("member_id") : null;
            // 创建 UnreadMessage 对象
            UnreadMessage message = new UnreadMessage(
                    userId,
                    senderId,
                    type,
                    messageType,
                    content,
                    sendTime,
                    messageId,
                    count,
                    memberId
            );

            // 将解析的消息添加到结果列表中
            result.add(message);
        }
        return result;
    }
}
