package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Post;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;

public class PostParser {

    public static List<Post> userDetailParser(ResultSet set) {
        ArrayList<Post> posts = new ArrayList<>();
        for (Row row:
                set.all()) {
            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
            if (columnDefinitions.contains("receiver_id")) {
                posts.add(new Post(!columnDefinitions.contains("user_id")?null:row.getString("user_id"),
                        !columnDefinitions.contains("user_email")?null:row.getString("user_email"),
                        !columnDefinitions.contains("user_name")?null:row.getString("user_name"),
                        !columnDefinitions.contains("intro")?null:row.getString("intro"),
                        !columnDefinitions.contains("avatar")?null:row.getString("avatar"),
                        !columnDefinitions.contains("birthdate")?null:row.getLocalDate("birthdate"),
                        !columnDefinitions.contains("telephone")?null:row.getString("telephone"),
                        !columnDefinitions.contains("gender") || row.getBoolean("gender"),
                        !columnDefinitions.contains("relationship")?0:row.getInt("relationship"),
                        !columnDefinitions.contains("apps")?null:row.getList("apps", String.class)));

            } else {
                posts.add(new Post());

            }
        }
        return posts;
    }


}
