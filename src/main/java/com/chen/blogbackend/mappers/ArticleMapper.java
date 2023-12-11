package com.chen.blogbackend.mappers;

import com.chen.blogbackend.DAO.ArticleDao;
import com.chen.blogbackend.entities.Article;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.DaoKeyspace;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface ArticleMapper {

    @DaoFactory
    ArticleDao getArticleDao();
}
