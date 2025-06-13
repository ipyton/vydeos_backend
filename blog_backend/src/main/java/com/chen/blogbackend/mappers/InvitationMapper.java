package com.chen.blogbackend.mappers;


import com.chen.blogbackend.DAO.FriendDao;
import com.chen.blogbackend.DAO.InvitationDao;
import com.chen.blogbackend.entities.Invitation;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

import java.util.LinkedList;
import java.util.List;


public class InvitationMapper {
    public static List<Invitation> parseInvitation(ResultSet resultSet) {
        return new LinkedList<>();
    }

}
