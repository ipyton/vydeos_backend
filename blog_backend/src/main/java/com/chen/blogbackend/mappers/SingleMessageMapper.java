package com.chen.blogbackend.mappers;

import com.chen.blogbackend.DAO.SettingDao;
import com.chen.blogbackend.DAO.SingleMessageDao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface SingleMessageMapper {

    @DaoFactory
    SingleMessageDao getDao();
}
