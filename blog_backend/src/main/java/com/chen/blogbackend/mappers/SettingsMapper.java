package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Settings;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingsMapper {
    public static Settings parseSettings(ResultSet rs) {
        Settings settings = new Settings();

        HashMap<String, String> map = new HashMap<>();
        String applicationId = null;
        String userId = null;
        for (Row row : rs) {
            userId = row.getString("user_id");
            if (applicationId == null) {
                applicationId = row.getString("application_id"); // 注意字段名是 "application_id"，要与数据库一致
                settings.setApplicationID(applicationId);
            }

            String key = row.getString("key");
            String value = row.getString("value");
            map.put(key, value);
        }
        settings.setUserId(userId);
        settings.setMap(map);
        return settings;
    }
}
