package com.chen.blogbackend.controllers;

import com.chen.blogbackend.entities.Article;

import com.chen.blogbackend.services.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@Controller("article")
public class ArticleController {

    @Autowired
    ArticleService service;


    @RequestMapping("get")
    public ArrayList<Article> getArticle(@RequestParam("author_id") String authorID) {
        return service.getArticles(authorID);
    }

    @RequestMapping("get")
    public ArrayList<Article> getArticle(@RequestParam("author_id") String authorID, @RequestParam("from") int from, @RequestParam("to") int to) {
        return service.getArticles(authorID, from, to);
    }
}
