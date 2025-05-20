package com.chen.blogbackend.DAO;

import com.chen.blogbackend.entities.ChatGroup;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PagingState;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.GetEntity;

@Dao
public interface ChatGroupDao {
    @GetEntity
    PagingIterable<ChatGroup> convert(ResultSet set);

}
