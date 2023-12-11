package com.chen.blogbackend.DAO;

import com.chen.blogbackend.entities.App;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.GetEntity;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;

import java.util.UUID;

@Dao
public interface AppDao {

    @GetEntity
    PagingIterable<App> convert(ResultSet set);

    @Insert
    void insert(App app);

}
