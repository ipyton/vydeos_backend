package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Setting;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
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

    PreparedStatement selectByUserIdAndApplication;
    PreparedStatement setSetting;
    PreparedStatement selectSettingsByUserId;
    PreparedStatement selectItemsByUserId;

    @PostConstruct
    public void init(){
        selectByUserIdAndApplication =  session.prepare("select * from setting_by_user where user_id = ? and applicationId = ?");
        setSetting = session.prepare("insert into settings_by_user values(?, ?, ?, ?)");
        selectSettingsByUserId = session.prepare("select * from setting_by_user where user_id=?");
        selectItemsByUserId = session.prepare("select applicationId where user_id=?");
    }


    public Setting getSettingByUserAndAppId(String userId, String applicationId) {
        ResultSet execute = session.execute(selectByUserIdAndApplication.bind(userId, applicationId));
        HashMap<String, String> map = new HashMap<>();
        for (Row row : execute) {
            for (ColumnDefinition definition: row.getColumnDefinitions()) {
                map.put(definition.getName().asInternal(), row.getString(definition.getName().asInternal()));
            }
        }
        return new Setting(userId, applicationId, map);
    }


    public Setting getSettingsByUser(String userId) {
        ResultSet execute = session.execute(selectSettingsByUserId.bind(userId));


    }


    public void set(String userId, Setting setting) {

        BatchStatementBuilder  builder = new BatchStatementBuilder(BatchType.LOGGED);
        for (Map.Entry<String,String> entry:setting.getMap().entrySet()) {
            builder.addStatement(setSetting.bind(userId, setting.getApplicationID(), entry.getKey(), entry.getValue()));
        }
        ResultSet result = session.execute(builder.build());
        result.getExecutionInfo().get

    }
}
