package com.chen.blogbackend.DAO;

import com.chen.blogbackend.entities.App;
import com.chen.blogbackend.entities.Article;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;

@Dao
public interface ArticleDao {

    @Insert
    void save(Article article);


    @Select
    PagingIterable<Article> findById(String applicationId);

}
