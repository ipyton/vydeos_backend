package com.chen.blogbackend.mappers;

import com.chen.blogbackend.DAO.SettingDao;
import com.chen.blogbackend.entities.Setting;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.GetEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SettingsMapper {

    @DaoFactory
    SettingDao getDao();


}
