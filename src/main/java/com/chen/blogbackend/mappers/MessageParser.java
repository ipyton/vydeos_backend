package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.GroupMessage;
import com.chen.blogbackend.entities.OnlineGroupMessage;
import com.chen.blogbackend.entities.SingleMessage;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;

public class MessageParser {
    public static List<SingleMessage> parseToSingleMessage(ResultSet set) {
        ArrayList<SingleMessage> result = new ArrayList<>();
        for (Row row: set.all()) {
            result.add(new SingleMessage(row.getString("message_id"), row.getString("user_id"), row.getString("receiver_id"),
            row.getString("type"), row.getInstant("send_time"), row.getString("content"),
                    row.getString("refer_message_id"), row.getList("refer_user_id", String.class), row.getString("messageType")));
        }
        System.out.println(result.size());
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
