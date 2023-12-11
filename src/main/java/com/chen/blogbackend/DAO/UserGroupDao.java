package com.chen.blogbackend.DAO;

import com.chen.blogbackend.entities.App;
import com.chen.blogbackend.entities.Setting;
import com.chen.blogbackend.entities.UserGroup;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.GetEntity;
import com.datastax.oss.driver.api.mapper.annotations.Insert;

@Dao
public interface UserGroupDao {
    @GetEntity
    PagingIterable<UserGroup> convert(ResultSet set);

    @Insert
    void save(UserGroup userGroup);
}
