package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.SettingDao;
import com.chen.blogbackend.entities.Settings;
import com.chen.blogbackend.mappers.SettingsMapper;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class SettingsService {

    private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

    @Autowired
    CqlSession session;

    PreparedStatement getUserSetting;
    PreparedStatement getUserSettings;
    PreparedStatement upsertUserSetting;
    PreparedStatement getGlobalSettings;
    PreparedStatement upsertGlobalSetting;

    @PostConstruct
    public void init(){
        logger.info("Initializing SettingsService and preparing CQL statements");

        try {
            getUserSettings = session.prepare("select * from userInfo.settings_by_user where user_id = ? and application_id = ?");
            getUserSetting = session.prepare("select * from userInfo.settings_by_user where user_id = ? and application_id = ?");
            upsertUserSetting = session.prepare("insert into userInfo.settings_by_user (application_id, user_id, key, value) values (?, ?, ?, ?)");
            getGlobalSettings = session.prepare("select * from userInfo.global_settings where application_id = ? and key = ?");
            upsertGlobalSetting = session.prepare("insert into userInfo.global_settings( application_id, key, value) values(?, ?, ?)");

            logger.info("Successfully prepared all CQL statements for SettingsService");
        } catch (Exception e) {
            logger.error("Failed to prepare CQL statements during SettingsService initialization", e);
            throw e;
        }
    }

    public Settings getUserSettings(String userId, String applicationId) {
        logger.debug("Retrieving user settings for userId: {} and applicationId: {}", userId, applicationId);

        try {
            ResultSet resultSet = session.execute(getUserSettings.bind(userId, applicationId));

            HashMap<String, String> map = new HashMap<>();
            int rowCount = 0;

            for (Row row : resultSet) {
                rowCount++;
                for (ColumnDefinition definition: row.getColumnDefinitions()) {
                    String columnName = definition.getName().asInternal();
                    String value = row.getString(columnName);
                    map.put(columnName, value);
                    logger.trace("Added setting: {} = {}", columnName, value);
                }
            }

            logger.debug("Retrieved {} settings rows for userId: {} and applicationId: {}",
                    rowCount, userId, applicationId);

            Settings settings = new Settings(userId, applicationId, map);
            logger.info("Successfully retrieved user settings for userId: {} and applicationId: {}",
                    userId, applicationId);

            return settings;

        } catch (Exception e) {
            logger.error("Failed to retrieve user settings for userId: {} and applicationId: {}",
                    userId, applicationId, e);
            throw e;
        }
    }

    public Settings getUserSetting(String applicationId, String userId, String key) {
        logger.debug("Retrieving specific user setting for userId: {}, applicationId: {}, key: {}",
                userId, applicationId, key);

        try {
            ResultSet execute = session.execute(getUserSetting.bind(userId, applicationId, key));
            Settings settings = SettingsMapper.parseSettings(execute);

            logger.info("Successfully retrieved user setting for userId: {}, applicationId: {}, key: {}",
                    userId, applicationId, key);

            return settings;

        } catch (Exception e) {
            logger.error("Failed to retrieve user setting for userId: {}, applicationId: {}, key: {}",
                    userId, applicationId, key, e);
            throw e;
        }
    }

    public boolean upsertUserSetting(String userId, String applicationId, String key, String value) {
        logger.debug("Upserting user setting for userId: {}, applicationId: {}, key: {}",
                userId, applicationId, key);

        try {
            session.execute(upsertUserSetting.bind(userId, applicationId, key, value));

            logger.info("Successfully upserted user setting for userId: {}, applicationId: {}, key: {}",
                    userId, applicationId, key);

            return true;

        } catch (Exception e) {
            logger.error("Failed to upsert user setting for userId: {}, applicationId: {}, key: {}",
                    userId, applicationId, key, e);
            throw e;
        }
    }

    public Settings getGlobalSettings(String applicationId, String key) {
        logger.debug("Retrieving global setting for applicationId: {}, key: {}", applicationId, key);

        try {
            ResultSet execute = session.execute(getGlobalSettings.bind(applicationId, key));
            Settings settings = SettingsMapper.parseSettings(execute);

            logger.info("Successfully retrieved global setting for applicationId: {}, key: {}",
                    applicationId, key);

            return settings;

        } catch (Exception e) {
            logger.error("Failed to retrieve global setting for applicationId: {}, key: {}",
                    applicationId, key, e);
            throw e;
        }
    }

    public boolean upsertGlobalSetting(String applicationId, String key, String value) {
        logger.debug("Upserting global setting for applicationId: {}, key: {}", applicationId, key);

        try {
            session.execute(upsertGlobalSetting.bind(applicationId, key, value));

            logger.info("Successfully upserted global setting for applicationId: {}, key: {}",
                    applicationId, key);

            return true;

        } catch (Exception e) {
            logger.error("Failed to upsert global setting for applicationId: {}, key: {}",
                    applicationId, key, e);
            throw e;
        }
    }
}