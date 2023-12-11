package com.chen.blogbackend.DAO;


import com.chen.blogbackend.entities.App;
import com.chen.blogbackend.entities.ApplicationComment;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.GetEntity;
import com.datastax.oss.driver.api.mapper.annotations.Insert;

@Dao
public interface ApplicationCommentDao {

    @GetEntity
    PagingIterable<App> convert(ResultSet set);

    @Insert
    void save(ApplicationComment comment);
}
