package com.chen.blogbackend.entities;

import java.time.Instant;
import java.util.List;

public class MovieDownloadRequest {
    public String movieId;
    public String userId;
    public Instant timestamp;
    public String movieName;
    public List<String> actorList;
    public String release_year;

    public MovieDownloadRequest(String movieId, String userId, Instant timestamp, String movieName,
                                List<String> actorList, String release_year) {
        this.movieId = movieId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.movieName = movieName;
        this.actorList = actorList;
        this.release_year = release_year;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public List<String> getActorList() {
        return actorList;
    }

    public void setActors(List<String> actorList) {
        this.actorList = actorList;
    }

    public String getYear() {
        return release_year;
    }

    public void setYear(String release_year) {
        this.release_year = release_year;
    }
}
