package com.chen.blogbackend.DAO;

import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.UserGroup;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.mapper.annotations.*;

@Dao
public interface FriendDao {

    @Insert
    void addFriend(Friend friend);

    @SetEntity(lenient = true)
    BoundStatement setEntity(Friend friend, BoundStatement boundStatement);

    @Query("select * from group_users where group_id = :id;")
    PagingIterable<Friend> selectGroupByID(String id);

    @Query("select * from followers_by_user_id where user_id = :id;")
    PagingIterable<Friend> selectUserFollows(String id);

    @Query("select user_id from followers_by_user_id where user_id = :id;")
    PagingIterable<String> selectUserIdsFollows(String id);


    @Query("select * from followers_by_idol where idol_id = :id;")
    PagingIterable<Friend> selectUserFollowers(String id);


    //BoundStatement bind(Friend friend, BoundStatement statement);

    @Query("delete from followers_by_user_id where user_id = :from;")
    void deleteForFan(String from, String to);

    @Query("delete from followers_by_idol_id whrer idol_id = :to;")
    void deleteForIdol(String from, String to);




}
