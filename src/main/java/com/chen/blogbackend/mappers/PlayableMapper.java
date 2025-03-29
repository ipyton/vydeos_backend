package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Playable;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;

public class PlayableMapper {

    public static List<Playable> parse(ResultSet execute) {

        ArrayList<Playable> playables = new ArrayList<>();
        for (Row row : execute.all()) {
            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
            playables.add(new Playable(
                    !columnDefinitions.contains("resource_id") ? null : row.getString("resource_id"),
                    !columnDefinitions.contains("type") ? null : row.getString("type"),
                    !columnDefinitions.contains("quality") ? null : row.getByte("quality"),
                    !columnDefinitions.contains("bucket") ? null : row.getString("bucket"),
                    !columnDefinitions.contains("path") ? null : row.getString("path")
            ));
        }
        return playables;

    }
}
