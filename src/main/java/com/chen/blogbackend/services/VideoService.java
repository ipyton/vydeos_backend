package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Post;
import com.chen.blogbackend.entities.Video;
import com.chen.blogbackend.mappers.VideoParser;
import com.chen.blogbackend.util.VideoUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

@Service
public class VideoService {


    @Autowired
    CqlSession session;

    @Autowired
    Jedis jedis;

    private PreparedStatement getVideoMeta;
    private PreparedStatement getGallery;
    private PreparedStatement collectVideo;
    private PreparedStatement removeVideo;


    @PostConstruct
    public void init() {
        getVideoMeta = session.prepare("select * from movie.meta where movieId = ?");
        getGallery = session.prepare("select * from movie.movieGallery where userId = ?");
        collectVideo = session.prepare("insert into movie.movieGallery(movieId, userId, poster, introduction, " +
                "movie_name, actress_list,release_year) values(?,?,?,?,?,?,?)");
        removeVideo = session.prepare("delete from movie.movieGallery where userId = ? and movieId = ?");
    }

    public boolean starVideo(Video video){

        return true;
    }

    public Video getVideo(){
        return new Video();
    }

    public Video getVideoMeta(String videoId) {
        System.out.println(videoId);
        ResultSet execute = session.execute(getVideoMeta.bind(videoId));
        List<Video> videos = VideoParser.videoMetaParser(execute);
        return videos.get(0);
    }

    // return user saved information.
    public List<Video> getGallery(String userId) {
        ResultSet execute = session.execute(getGallery.bind(userId));
        return VideoParser.videoMetaParser(execute);
    }

    public boolean collectVideo(String userId, String videoId) {
        Video videoMeta = getVideoMeta(videoId);

        ResultSet execute = session.execute(collectVideo.bind(videoId, userId, videoMeta.getPoster(),
                videoMeta.getIntroduction(),videoMeta.getMovie_name(),videoMeta.getActress_list(),videoMeta.getRelease_year()));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean deleteVideo(String userId, String videoId) {
        ResultSet execute = session.execute(removeVideo.bind(userId, videoId));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }



}
