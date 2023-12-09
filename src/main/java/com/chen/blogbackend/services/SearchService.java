package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.SearchResult;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    @Autowired
    SqlSessionFactory sqlSessionFactory;


    public SearchResult searchByContent(String userId, String text) {


        return new SearchResult();
    }

    public SearchResult searchByUser(String userId) {
        return  new SearchResult();
    }

    public boolean updateIndex(SearchResult result) {
        return true;
    }







}
