package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Article;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;

@Service
public class ArticleService {
    public ArrayList<Article> getArticles(String userID, int from ,int to) {
        return new ArrayList<>();
    }

    public ArrayList<Article> getArticles(String userID) {
        return new ArrayList<>();
    }

    public boolean setArticle(Article article){
        return true;
    }

}
