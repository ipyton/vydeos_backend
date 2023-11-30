package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Video;
import org.springframework.stereotype.Service;

@Service
public class VideoService {

    public boolean addVideo(Video video){
        return true;
    }

    public Video getVideo(){
        return new Video();
    }

    public Video getVideoInformation() {
        return new Video();
    }




}
