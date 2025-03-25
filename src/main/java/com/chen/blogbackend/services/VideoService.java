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
    private PreparedStatement unstarVideo;

    private PreparedStatement sendRequest;
    private PreparedStatement getRequest;
    private PreparedStatement getRequestById;
    private PreparedStatement isStared;
//movieId text, createTime timestamp, userId text
    @PostConstruct
    public void init() {
        getVideoMeta = session.prepare("select * from movie.meta where resource_id = ? and type = ? and language = ?;");
        getGallery = session.prepare("select * from movie.movieGallery where user_id = ?;");
        collectVideo = session.prepare("insert into movie.movieGallery(resource_id, user_id, poster, introduction, " +
                "movie_name, actor_list,release_year,language,type) values(?,?,?,?,?,?,?,?,?);");
        unstarVideo = session.prepare("delete from movie.movieGallery where user_id = ? and resource_id = ? and type = ? ;");
        sendRequest = session.prepare("insert into movie.requests (resource_id, type, create_time, userId, " +
                "movie_name, actor_list, release_year,language) values (?, ?, ?, ?, ?, ?, ?, ?);");
        getRequest = session.prepare("select * from movie.requests;");
        getRequestById = session.prepare("select * from movie.requests where resource_id = ? and type = ?;");
        isStared = session.prepare("select * from movie.movieGallery where user_id = ? and resource_id = ? and type = ? ;");
    }

    public boolean starVideo(Video video){
        return true;
    }

    public Video getVideo(){
        return new Video();
    }

    public Video getVideoMeta(String resourceId, String type ,String language) {
        ResultSet execute = session.execute(getVideoMeta.bind(resourceId, type, language));
        List<Video> videos = VideoParser.videoMetaParser(execute);
        return videos.isEmpty() ? null : videos.get(0);
    }

    // return user saved information.
    public List<Video> getGallery(String userId) {
        ResultSet execute = session.execute(getGallery.bind(userId));
        return VideoParser.videoMetaParser(execute);
    }

    public boolean collectVideo(String userId, String videoId, String type, String language) {
        Video videoMeta = getVideoMeta(videoId, type, language);

        ResultSet execute = session.execute(collectVideo.bind(videoId, userId, videoMeta.getPoster(),
                videoMeta.getIntroduction(),videoMeta.getMovieName(),videoMeta.getActorList(),
                videoMeta.getReleaseYear(), language, type));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean unstarVideo(String userId, String resourceId, String type) {
        ResultSet execute = session.execute(unstarVideo.bind(userId, resourceId, type));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean sendRequest(String email, String resourceId, String type, String language) {
        ResultSet execute1 = session.execute(getVideoMeta.bind(resourceId,type, "en-US"));
        List<Video> videos = VideoParser.videoMetaParser(execute1);
        if (videos.isEmpty()) {
            System.out.println("no videos meta found for " + type + " " +  resourceId + " " + email + " " + language );
            return false;
        }
        Video video = videos.get(0);
        // movieId, create_time, userId, movie_name, actor_list, release_year
        ResultSet execute = session.execute(sendRequest.bind(video.getResourceId(), type, Instant.now() ,email,
                video.getMovieName(),video.getActorList(),video.getReleaseYear(),"en-US"));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }

    public List<MovieDownloadRequest> getRequests() {
        ResultSet execute = session.execute(getRequest.bind());
        return MovieDownloadRequestParser.parseMovieDownloadRequest(execute);
    }

    public boolean isRequested(String movieId, String type) {
        ResultSet execute = session.execute(getRequestById.bind(movieId, type));
        return !execute.all().isEmpty();
    }

    public boolean isStared(String userId, String resourceId, String type) {
        ResultSet execute = session.execute(isStared.bind(userId, resourceId, type));
        return !execute.all().isEmpty();
    }
}
