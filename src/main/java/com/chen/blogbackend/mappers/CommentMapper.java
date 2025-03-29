package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Comment;
import com.chen.blogbackend.entities.Country;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.LinkedList;
import java.util.List;

public class CommentMapper {
    public static List<Comment> commentsMapper(ResultSet resultSet) {
        List<Comment> comments = new LinkedList<>();
        for (Row row : resultSet) {
            comments.add(commentMapper(row));
        }
        return comments;
    }

    public static Comment commentMapper(Row row) {
        ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
        //resource_id,type,time,user_id,content,likes
        return new Comment(
                columnDefinitions.contains("user_id") ? row.getString("user_id") : null,
                columnDefinitions.contains("type") ? row.getString("type") : null,
                columnDefinitions.contains("resource_id") ? row.getString("resource_id") : null,
                columnDefinitions.contains("comment_id") ? row.getString("comment_id") : null,
                columnDefinitions.contains("content") ? row.getString("content") : null,
                columnDefinitions.contains("likes") ? row.getLong("likes") : 0,
                columnDefinitions.contains("time") ? row.getInstant("time") : null
        );
    }
}
