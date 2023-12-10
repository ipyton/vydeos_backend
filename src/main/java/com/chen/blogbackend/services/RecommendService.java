package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Article;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.Video;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class RecommendService {

    public ArrayList<Article> getRecommendArticles() {
        return new ArrayList<>();
    }

    public ArrayList<Video> getRecommendVideos() {
        return new ArrayList<>();
    }

    public ArrayList<Friend> getRecommendFriend() {
        return new ArrayList<>();
    }

}
