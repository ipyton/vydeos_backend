package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Post;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.Video;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class RecommendService {

    public ArrayList<Post> getRecommendArticles() {
        return new ArrayList<>();
    }

    public ArrayList<Video> getRecommendVideos() {
        return new ArrayList<>();
    }

    public ArrayList<Friend> getRecommendFriend() {
        return new ArrayList<>();
    }

}
