package com.chen.blogbackend.DAO;

import com.chen.blogbackend.entities.deprecated.SingleMessage;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.GetEntity;

@Dao
public interface SingleMessageDao {
    @GetEntity
    PagingIterable<SingleMessage> convert(ResultSet set);

}
