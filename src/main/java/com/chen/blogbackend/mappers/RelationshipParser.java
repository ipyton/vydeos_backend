package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Relationship;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;

public class RelationshipParser {
    public static List<Relationship> parseToRelationship(ResultSet set) {
        ArrayList<Relationship> result = new ArrayList<>();
        for (Row row: set.all()) {
            result.add(new Relationship(row.getString("user_id"), row.getString("friend_id"),
                    set.getColumnDefinitions().contains("avatar")?row.getString("avatar"):null, row.getString("group_id"), row.getString("name")));
        }
        return result;


    }
}
