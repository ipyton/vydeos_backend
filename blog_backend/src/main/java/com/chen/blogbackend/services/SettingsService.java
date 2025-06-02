package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.SettingDao;
import com.chen.blogbackend.entities.Settings;
import com.chen.blogbackend.mappers.SettingsMapper;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class SettingsService {
    @Autowired
    CqlSession session;

    PreparedStatement getUserSetting;
    PreparedStatement getUserSettings;
    PreparedStatement upsertUserSetting;
    PreparedStatement getGlobalSettings;
    PreparedStatement upsertGlobalSetting;

    @PostConstruct
    public void init(){
        getUserSettings = session.prepare("select * from userInfo.settings_by_user where user_id = ? and application_id = ?");
        getUserSetting = session.prepare("select * from userInfo.settings_by_user where user_id = ? and application_id = ?");
        upsertUserSetting = session.prepare("insert into userInfo.settings_by_user (application_id, user_id, key, value) values (?, ?, ?, ?)");
        getGlobalSettings = session.prepare("select * from userInfo.global_settings where application_id = ? and key = ?");
        upsertGlobalSetting = session.prepare("insert into userInfo.global_settings( application_id, key, value) values(?, ?, ?)");
    }

    public Settings getUserSettings(String userId, String applicationId) {
        ResultSet resultSet = session.execute(getUserSettings.bind(userId, applicationId));

        HashMap<String, String> map = new HashMap<>();
        for (Row row : resultSet) {
            for (ColumnDefinition definition: row.getColumnDefinitions()) {
                map.put(definition.getName().asInternal(), row.getString(definition.getName().asInternal()));
            }
        }
        return new Settings(userId, applicationId, map);
    }

    public Settings getUserSetting(String applicationId,String userId, String key) {
        ResultSet execute = session.execute(getUserSetting.bind(userId, applicationId, key));
        Settings settings = SettingsMapper.parseSettings(execute);
        return settings;
    }


    public boolean upsertUserSetting(String userId, String applicationId, String key, String value) {
        session.execute(upsertUserSetting.bind(userId, applicationId, key, value));
        return true;
    }

    public Settings getGlobalSettings(String applicationId, String key) {
        ResultSet execute = session.execute(getGlobalSettings.bind(applicationId, key));
        Settings settings = SettingsMapper.parseSettings(execute);
        return settings;
    }

    public boolean upsertGlobalSetting(String applicationId, String key, String value) {
        session.execute(upsertGlobalSetting.bind(applicationId, key, value));
        return true;
    }

}
