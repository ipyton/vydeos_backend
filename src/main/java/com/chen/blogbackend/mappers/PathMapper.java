package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Path;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;

public class PathMapper {
    public static List<Path> parsePaths(ResultSet execute) {

        ArrayList<Path> result = new ArrayList<>();
        for (Row row : execute.all()) {
            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
            result.add(new Path(
                    !columnDefinitions.contains("roleId") ? null : row.getInt("roleId"),
                    !columnDefinitions.contains("role_name") ? null : row.getString("role_name"),
                    !columnDefinitions.contains("path") ? null : row.getString("path"),
                    !columnDefinitions.contains("name") ? null : row.getString("name"),
                    !columnDefinitions.contains("version") ? null : row.getInt("version"),
                    !columnDefinitions.contains("icon_name") ? null : row.getString("icon_name"),
                    !columnDefinitions.contains("type") ? null : row.getString("type")
            ));
        }
        return result;
    }
}
