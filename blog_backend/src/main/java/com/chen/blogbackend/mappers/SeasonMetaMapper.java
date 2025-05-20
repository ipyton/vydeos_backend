package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.SeasonMeta;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;

public class SeasonMetaMapper {
    public static List<SeasonMeta> parseSeasonMeta(ResultSet execute) {

        ArrayList<SeasonMeta> seasonMetas = new ArrayList<>();
        for (Row row : execute.all()) {
            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
            seasonMetas.add(new SeasonMeta(
                    !columnDefinitions.contains("resource_id") ? null : row.getString("resource_id"),
                    !columnDefinitions.contains("type") ? null : row.getString("type"),
                    !columnDefinitions.contains("total_episode") ? null : row.getInt("total_episode"),
                    !columnDefinitions.contains("season_id") ? null : row.getInt("season_id")
            ));
        }
        return seasonMetas;
    }
}
