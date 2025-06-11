package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.ChatGroup;
import com.chen.blogbackend.entities.GroupUser;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GroupParser {
    public static List<GroupUser> groupListParser(ResultSet resultSet)  {
        List<GroupUser> groupUsers = new ArrayList<>();

        // Iterate through the rows of the ResultSet

        for (Row row: resultSet.all()) {
            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
            //String userId = row.getString("user_id");
            Long groupId = columnDefinitions.contains("group_id") ? row.getLong("group_id") : null;
            String userId = columnDefinitions.contains("user_id") ? row.getString("user_id") : null;
            String userName = columnDefinitions.contains("user_name") ? row.getString("user_name") : null ;
            String groupName = columnDefinitions.contains("group_name") ? row.getString("group_name") : null;

            // Create a new GroupUser object and add it to the list
            GroupUser groupUser = new GroupUser(userId, groupId, groupName, userName);
            groupUsers.add(groupUser);
        }

        return groupUsers;
    }
    public static ChatGroup parseDetails(ResultSet resultSet) {
        Row row = resultSet.one();
        if (row != null) {
            long groupIdLong = row.getColumnDefinitions().contains("group_id") && !row.isNull("group_id")
                    ? row.getLong("group_id") : 0L;

            String groupName = row.getColumnDefinitions().contains("name") && !row.isNull("name")
                    ? row.getString("name") : null;

            String groupDescription = row.getColumnDefinitions().contains("introduction") && !row.isNull("introduction")
                    ? row.getString("introduction") : null;

            String owner = row.getColumnDefinitions().contains("owner_id") && !row.isNull("owner_id")
                    ? row.getString("owner_id") : null;

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
