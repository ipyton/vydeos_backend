package com.chen.blogbackend.DAO;

import com.chen.blogbackend.entities.UnfinishedUpload;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;

import java.util.List;

@Dao
public interface VideoUploadingDao {

    @Select
    UnfinishedUpload findById(String fileHash);

    @Select
    List<UnfinishedUpload> getAll();

    @Insert
    void update(UnfinishedUpload unfinishedUpload);
}
