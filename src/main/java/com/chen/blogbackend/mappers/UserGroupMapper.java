package com.chen.blogbackend.mappers;

import com.chen.blogbackend.DAO.UserGroupDao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface UserGroupMapper {

    @DaoFactory
    UserGroupDao getDao();

}
