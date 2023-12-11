package com.chen.blogbackend.mappers;

import com.chen.blogbackend.DAO.CommentDao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface CommentMapper {

    @DaoFactory
    CommentDao getDao();
}
