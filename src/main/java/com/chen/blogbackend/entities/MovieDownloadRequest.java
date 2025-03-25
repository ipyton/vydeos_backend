package com.chen.blogbackend.entities;

import java.time.Instant;
import java.util.List;

public class MovieDownloadRequest {
    public String resourceId;
    public String userId;
    public Instant timestamp;
    public String movieName;
    public List<String> actorList;
    public String release_year;
    public String type;

    public MovieDownloadRequest(String resourceId, String userId, Instant timestamp, String movieName,
                                List<String> actorList, String release_year,String type) {
        this.resourceId = resourceId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.movieName = movieName;
        this.actorList = actorList;
        this.release_year = release_year;
        this.type = type;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setActorList(List<String> actorList) {
        this.actorList = actorList;
    }

    public String getRelease_year() {
        return release_year;
    }

    public void setRelease_year(String release_year) {
        this.release_year = release_year;
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
