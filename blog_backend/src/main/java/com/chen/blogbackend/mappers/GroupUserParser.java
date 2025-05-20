package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.GroupUser;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;

public class GroupUserParser {
    public static List<GroupUser> groupUserParser(ResultSet resultSet)  {
        List<GroupUser> groupUsers = new ArrayList<>();

        // Iterate through the rows of the ResultSet

        for (Row row: resultSet.all()) {

            String userId = row.getString("user_id");
            long groupId = row.getLong("group_id");
            String groupName = row.getString("group_name");

            // Create a new GroupUser object and add it to the list
            GroupUser groupUser = new GroupUser(userId, groupId, groupName);
            groupUsers.add(groupUser);
        }

        return groupUsers;
    }


}
