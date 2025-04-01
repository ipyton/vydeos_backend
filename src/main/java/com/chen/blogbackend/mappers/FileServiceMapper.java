package com.chen.blogbackend.mappers;


import com.chen.blogbackend.entities.FileUploadStatus;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.springframework.stereotype.Service;

import java.util.List;


public class FileServiceMapper {
    public static FileUploadStatus parseUploadStatus(ResultSet set) {
        List<Row> all = set.all();
        if (all.isEmpty()) {
            return null;
        }
        Row row = all.get(0);
        ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
        return new FileUploadStatus(
                !columnDefinitions.contains("user_email") ? null : row.getString("user_email"),
                !columnDefinitions.contains("fileName") ? null : row.getString("fileName"),
                !columnDefinitions.contains("resourceId") ? null : row.getLong("resourceId"),
                !columnDefinitions.contains("resourceType") ? null : row.getString("resourceType"),
                !columnDefinitions.contains("whole_hash") ? null : row.getString("whole_hash"),
                !columnDefinitions.contains("total_slices") ? null : row.getInt("total_slices"),
                !columnDefinitions.contains("current_slice") ? null : row.getInt("current_slice"),
                !columnDefinitions.contains("resource_name") ? null : row.getString("resource_name"),
                !columnDefinitions.contains("quality") ? null : row.getShort("quality"),
                !columnDefinitions.contains("status_code") ? null : row.getInt("status_code"),
                !columnDefinitions.contains("format") ? null : row.getString("format"),
                !columnDefinitions.contains("size") ? null : row.getLong("size"),
                !columnDefinitions.contains("seasonId") ? null : row.getInt("seasonId"),
        !columnDefinitions.contains("episode") ? null : row.getInt("episode")
                );
    }
}
