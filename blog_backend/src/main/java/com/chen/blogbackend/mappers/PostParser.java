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
                posts.add(new Post(!columnDefinitions.contains("author_id")?null:row.getString("author_id"),
                        !columnDefinitions.contains("author_name")?null:row.getString("author_name"),
                        !columnDefinitions.contains("post_id")?null:row.getLong("post_id"),
                        !columnDefinitions.contains("last_modified")?null:row.getInstant("last_modified"),
                        !columnDefinitions.contains("content")?null:row.getString("content"),
                        !columnDefinitions.contains("likes")?0:row.getInt("likes"),
                        !columnDefinitions.contains("notice")?null:row.getList("notice", String.class),
                        !columnDefinitions.contains("accessRules")?null:row.getList("accessRules", String.class),
                        !columnDefinitions.contains("images")?null:row.getList("images", String.class),
                        !columnDefinitions.contains("voices")?null:row.getList("voices", String.class),
                        !columnDefinitions.contains("videos")?null:row.getList("videos",String.class),
                        !columnDefinitions.contains("comments")?null:row.getList("comments", String.class),
                        row.getString("receiver_id"),
                        row.getString("location")));
            } else {
                posts.add(new Post(!columnDefinitions.contains("author_id")?null:row.getString("author_id"),
                        !columnDefinitions.contains("author_name")?null:row.getString("author_name"),
                        !columnDefinitions.contains("post_id")?null:row.getLong("post_id"),
                        !columnDefinitions.contains("last_modified")?null:row.getInstant("last_modified"),
                        !columnDefinitions.contains("content")?null:row.getString("content"),
                        !columnDefinitions.contains("likes")?0:row.getInt("likes"),
                        !columnDefinitions.contains("notice")?null:row.getList("notice", String.class),
                        !columnDefinitions.contains("accessRules")?null:row.getList("accessRules", String.class),
                        !columnDefinitions.contains("images")?null:row.getList("images", String.class),
                        !columnDefinitions.contains("voices")?null:row.getList("voices", String.class),
                        !columnDefinitions.contains("videos")?null:row.getList("videos",String.class),
                        !columnDefinitions.contains("comments")?null:row.getList("comments", String.class),
                        row.getString("location")));

            }
        }
        return posts;
    }


}
