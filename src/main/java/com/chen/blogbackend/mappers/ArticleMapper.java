package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;

@Mapper
public interface ArticleMapper {

    @Select("select")
    ArrayList<Article> getArticles(String articleID);

    ArrayList<Article> getArticlesByUserID(String userID);

}
