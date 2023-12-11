package com.chen.blogbackend.mappers;

import com.chen.blogbackend.DAO.SettingDao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SettingsMapper {

    @DaoFactory
    SettingDao getDao();
}
