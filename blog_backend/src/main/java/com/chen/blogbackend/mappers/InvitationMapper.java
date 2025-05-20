package com.chen.blogbackend.mappers;


import com.chen.blogbackend.DAO.FriendDao;
import com.chen.blogbackend.DAO.InvitationDao;
import com.chen.blogbackend.entities.Invitation;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface InvitationMapper {
    @DaoFactory
    InvitationDao getDao();

}
