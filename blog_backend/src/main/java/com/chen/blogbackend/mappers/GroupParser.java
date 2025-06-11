package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.GroupUser;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;

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


}
