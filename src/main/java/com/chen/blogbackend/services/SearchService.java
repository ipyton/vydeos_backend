package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.App;
import com.chen.blogbackend.entities.SearchResult;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SearchService {

    @Autowired
    SqlSessionFactory sqlSessionFactory;


    public ArrayList<App> searchApplicationByName(String name){

        return new ArrayList<>();
    }

    public ArrayList<App> searchApplicationByDescription(String description){


        return new ArrayList<>();
    }

    public SearchResult searchByArticle(String userId, String text) {
        return new SearchResult();
    }

    public SearchResult searchByUser(String userId) {
        return new SearchResult();
    }

    public boolean updateIndex(SearchResult result) {
        return true;
    }







}
