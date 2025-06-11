package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public class ChatGroupParser {
    public static ChatGroup parseDetails(ResultSet resultSet) {
        Row row = resultSet.one();
        if (row != null) {
            long groupIdLong = row.getColumnDefinitions().contains("group_id") && !row.isNull("group_id")
                    ? row.getLong("group_id") : 0L;

            String groupName = row.getColumnDefinitions().contains("group_name") && !row.isNull("group_name")
                    ? row.getString("group_name") : null;

            String groupDescription = row.getColumnDefinitions().contains("group_description") && !row.isNull("group_description")
                    ? row.getString("group_description") : null;

            String owner = row.getColumnDefinitions().contains("owner") && !row.isNull("owner")
                    ? row.getString("owner") : null;

            Map<String, String> config = row.getColumnDefinitions().contains("config") && !row.isNull("config")
                    ? row.getMap("config", String.class, String.class) : Collections.emptyMap();

            String avatar = row.getColumnDefinitions().contains("avatar") && !row.isNull("avatar")
                    ? row.getString("avatar") : null;

            Instant createDatetime = row.getColumnDefinitions().contains("create_time") && !row.isNull("create_time")
                    ? row.getInstant("create_time") : Instant.EPOCH;

            boolean allowInviteByToken = row.getColumnDefinitions().contains("allow_invite_by_token") && !row.isNull("allow_invite_by_token")
                    ? row.getBoolean("allow_invite_by_token") : false;

            return new ChatGroup(groupIdLong, groupName, groupDescription, createDatetime,
                    owner, config, avatar, allowInviteByToken);
        } else {
            return null;
        }
    }

}
