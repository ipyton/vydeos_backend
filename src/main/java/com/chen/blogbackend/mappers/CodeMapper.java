package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Comment;
import com.chen.blogbackend.entities.Verification;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.List;

public class CodeMapper {

    public static Verification codeMapper(ResultSet resultSet) {
        List<Row> all = resultSet.all();
        if (all.size() == 0) return null;
        Row row = all.get(0);
        ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
        //resource_id,type,time,user_id,content,likes
        return new Verification(
                columnDefinitions.contains("userid") ? row.getString("userid") : null,
                columnDefinitions.contains("code") ? row.getString("code") : null,
                columnDefinitions.contains("expire_time") ? row.getInstant("expire_time"):null
        );
    }
}
