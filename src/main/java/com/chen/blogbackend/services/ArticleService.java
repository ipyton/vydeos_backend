package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Article;
import com.datastax.driver.mapping.DefaultPropertyMapper;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

import com.datastax.driver.mapping.PropertyMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;

@Service
public class ArticleService {
    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    MappingManager manager;

    public ArrayList<Article> getArticles(String userEmail, int from ,int to) {

        Mapper<Article> articleMapper = manager.mapper(Article.class);
        articleMapper.get();
        return new ArrayList<>();
    }

    public int  uploadArticle(String userEmail, Article article){
        return 1;

    }

    public Article getArticleByArticleID(String articleID) {

        return new Article();
    }

    public ArrayList<Article> getArticlesByFollowingAndFriends(String userEmail,int from, int to){
        ArrayList<Article> result = new ArrayList<>();
        ArrayList<Article> articlesFollowing = getArticlesFollowing(userEmail, from, to);
        ArrayList<Article> articlesFriends = getArticlesOfFriends(userEmail, from , to);
        return new ArrayList<>();
    }

    public ArrayList<Article> getArticlesFollowing(String userEmail, int from, int to) {
        return new ArrayList<>();
    }

    public ArrayList<Article> getArticlesOfFriends(String userEmail, int from, int to) {
        return new ArrayList<>();
    }

}
