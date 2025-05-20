package com.chen.blogbackend.mappers;

import com.chen.blogbackend.DAO.UserGroupDao;
import com.chen.blogbackend.DAO.VideoUploadingDao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface VideoUploadingMapper {

    @DaoFactory
    public VideoUploadingDao getDao();

}
