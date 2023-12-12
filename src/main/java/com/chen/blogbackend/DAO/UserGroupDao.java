package com.chen.blogbackend.DAO;

import com.chen.blogbackend.entities.App;
import com.chen.blogbackend.entities.Setting;
import com.chen.blogbackend.entities.UserGroup;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.GetEntity;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Query;

@Dao
public interface UserGroupDao {
    @GetEntity
    PagingIterable<UserGroup> convert(ResultSet set);

    @Query("select * from user_groups where group_id = :id;")
    PagingIterable<UserGroup> selectGroupByGroupID(String id);

    @Query("select friend_id from user_groups where user_id = :id;")
    PagingIterable<String> selectUserIdByGroupID(String id);

    @Query("select friend_id from user_owned_groups where user_id = :id;")
    PagingIterable<UserGroup> selectGroupsByUserId(String userId);


    @Query("insert into user_users values(:userId, :groupId, :avatar, :introduction)")
    void userJoinForGroup(String userId, String groupId, String avatar, String introduction);

    @Query("insert into group_users values(:groupId, :userId, :avatar, :introduction)")
    void userJoinForUser(String userId, String groupId,String avatar, String introduction);




}
