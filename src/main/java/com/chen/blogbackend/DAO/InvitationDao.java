package com.chen.blogbackend.DAO;

import com.chen.blogbackend.entities.Invitation;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;

@Dao
public interface InvitationDao {

    @Insert
    void insert(Invitation invitation);

    @Delete
    void delete(String invitationId);


    @Select
    Invitation select(String invitationId);




}
