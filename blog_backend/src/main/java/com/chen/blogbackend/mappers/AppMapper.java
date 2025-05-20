package com.chen.blogbackend.mappers;

import com.chen.blogbackend.DAO.AppDao;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.mapper.annotations.*;

@Mapper
public interface AppMapper {
    @DaoFactory
    AppDao appDao();

}
