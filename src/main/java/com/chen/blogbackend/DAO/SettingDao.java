package com.chen.blogbackend.DAO;

import com.chen.blogbackend.entities.App;
import com.chen.blogbackend.entities.Article;
import com.chen.blogbackend.entities.Setting;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.GetEntity;
import com.datastax.oss.driver.api.mapper.annotations.Insert;

@Dao
public interface SettingDao {
    @GetEntity
    PagingIterable<App> convert(ResultSet set);

    @Insert
    void save(Setting setting);
}
