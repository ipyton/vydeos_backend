package com.chen.blogbackend.DAO;

import com.chen.blogbackend.entities.Post;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.GetEntity;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;

@Dao
public interface ArticleDao {

    @Insert
    void save(Post post);


    @Select
    Post findById(String articleId);

    @GetEntity
    PagingIterable<Post> getArticles(ResultSet set);



}
