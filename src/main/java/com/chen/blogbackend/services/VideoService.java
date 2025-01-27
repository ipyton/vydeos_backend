package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.MovieDownloadRequest;
import com.chen.blogbackend.entities.Post;
import com.chen.blogbackend.entities.Video;
import com.chen.blogbackend.mappers.MovieDownloadRequestParser;
import com.chen.blogbackend.mappers.VideoParser;
import com.chen.blogbackend.util.VideoUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
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

    private PreparedStatement sendRequest;
    private PreparedStatement getRequest;
    private PreparedStatement getRequestById;
//movieId text, createTime timestamp, userId text
    @PostConstruct
    public void init() {
        getVideoMeta = session.prepare("select * from movie.meta where movieId = ?;");
        getGallery = session.prepare("select * from movie.movieGallery where userId = ?;");
        collectVideo = session.prepare("insert into movie.movieGallery(movieId, userId, poster, introduction, " +
                "movie_name, actress_list,release_year) values(?,?,?,?,?,?,?);");
        removeVideo = session.prepare("delete from movie.movieGallery where userId = ? and movieId = ?;");
        sendRequest = session.prepare("insert into movie.requests (movieId, create_time, userId, movie_name, actor_list, release_year) values (?, ?, ?, ?, ?, ?);");
        getRequest = session.prepare("select * from movie.requests;");
        getRequestById = session.prepare("select * from movie.requests where movieId = ?");
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
                videoMeta.getIntroduction(),videoMeta.getMovieName(),videoMeta.getActorList(),videoMeta.getReleaseYear()));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean deleteVideo(String userId, String videoId) {
        ResultSet execute = session.execute(removeVideo.bind(userId, videoId));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean sendRequest(String email, String videoId) {
        ResultSet execute1 = session.execute(getVideoMeta.bind(videoId));
        List<Video> videos = VideoParser.videoMetaParser(execute1);
        if (videos.isEmpty()) {
            return false;
        }
        Video video = videos.get(0);
        // movieId, create_time, userId, movie_name, actor_list, release_year
        ResultSet execute = session.execute(sendRequest.bind(video.getMovieId(),Instant.now() ,email, video.getMovieName(),video.getActorList(),video.getReleaseYear()));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }

    public List<MovieDownloadRequest> getRequests() {
        ResultSet execute = session.execute(getRequest.bind());
        return MovieDownloadRequestParser.parseMovieDownloadRequest(execute);
    }

    public boolean isRequested(String movieId) {
        ResultSet execute = session.execute(getRequestById.bind(movieId));
        return !execute.all().isEmpty();
    }
}
