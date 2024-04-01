package com.chen.blogbackend.mappers;

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
            row.getString("type"), row.getInstant(""), row.getString("content"), row.getString("media"),
                    row.getString("refer_message_id"), row.getList("refer_user_ids", String.class), row.getString("messageType")));
        }
        return result;
    }
}
