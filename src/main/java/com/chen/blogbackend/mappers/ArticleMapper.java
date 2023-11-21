package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Article;

import java.util.ArrayList;

public interface ArticleMapper {
    ArrayList<Article> getArticles(String articleID);

    ArrayList<Article> getArticlesByUserID(String userID);



}
