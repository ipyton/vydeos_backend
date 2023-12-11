package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.SettingDao;
import com.chen.blogbackend.entities.Setting;
import com.chen.blogbackend.mappers.SettingsMapper;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.metadata.Node;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettingsService {
    @Autowired
    CqlSession session;

    @Autowired
    SettingsService service;


    PreparedStatement selectByUserIdAndApplication;
    PreparedStatement selectSettingsByUserId;
    PreparedStatement selectItemsByUserId;

    SettingDao settingDao;

    @PostConstruct
    public void init(){
        selectByUserIdAndApplication =  session.prepare("select * from setting_by_user where user_id = ? and applicationId = ?");
        selectSettingsByUserId = session.prepare("select * from setting_by_user where user_id=?");
        selectItemsByUserId = session.prepare("select applicationId where user_id=?");

    }

    public Setting getSettingByUserAndAppId(String userId, String applicationId) {
        ResultSet resultSet = session.execute(selectByUserIdAndApplication.bind(userId, applicationId));

        HashMap<String, String> map = new HashMap<>();
        for (Row row : resultSet) {
            for (ColumnDefinition definition: row.getColumnDefinitions()) {
                map.put(definition.getName().asInternal(), row.getString(definition.getName().asInternal()));
            }
        }
        return new Setting(userId, applicationId, map);
    }

    public ArrayList<Setting> getSettingsByUser(String userId) {
        ResultSet execute = session.execute(selectSettingsByUserId.bind(userId));
        ArrayList<Setting> settings = new ArrayList<>();
        for (Row row : execute) {
            HashMap<String, String> hashmap = new HashMap<>();
            Setting setting = new Setting();
            setting.setName(userId);
            setting.setApplicationID(row.getString("applicationId"));
            for (ColumnDefinition definition : row.getColumnDefinitions()) {
                if (!definition.getName().asInternal().equals("applicationId")) {
                    hashmap.put(definition.getName().asInternal(), row.getString(definition.getName().asInternal()));
                }
            }
            setting.setMap(hashmap);

        }
        return settings;
    }


    public boolean set(String userId, ArrayList<Setting> settings) {
        BatchStatementBuilder  builder = new BatchStatementBuilder(BatchType.LOGGED);

        return true;
    }
}
