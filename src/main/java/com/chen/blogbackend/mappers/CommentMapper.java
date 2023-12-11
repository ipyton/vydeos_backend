package com.chen.blogbackend.mappers;

import com.chen.blogbackend.DAO.CommentDao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper {

    @DaoFactory
    CommentDao getDao();
}
