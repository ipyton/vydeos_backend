package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Article;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;

@Service
public class ArticleService {

    public ArrayList<Article> getArticles(String userEmail, int from ,int to) {
        return new ArrayList<>();
    }

    public boolean setArticle(Article article){
        return true;
    }


    public ArrayList<Article> getArticlesByFollowingAndFriends(String userEmail){
        ArrayList<Article> result = new ArrayList<>();
        ArrayList<Article> articlesFollowing= getArticlesFollowing(userEmail);
        ArrayList<Article> articlesFriends = getArticlesOfFriends(userEmail);



        return new ArrayList<>();
    }


    public ArrayList<Article> getArticlesFollowing(String userEmail) {
        return new ArrayList<>();
    }

    public ArrayList<Article> getArticlesOfFriends(String userEmail) {
        return new ArrayList<>();

    }

}
