package com.chen.blogbackend.mappers;

import com.chen.blogbackend.DAO.CommentDao;
import com.chen.blogbackend.DAO.FriendDao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface FriendMapper {
    @DaoFactory
    FriendDao getDao();


}
