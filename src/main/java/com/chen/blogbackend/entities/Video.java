package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.List;
import java.util.Map;

@Entity
public class Video {

    private String movieId;
    private String poster;
    private String score;
    private String introduction;
    private String movie_name;
    private String tags;
    private List<String> actress_list;
    private String release_year ;
    private String level;
    private List<String> picture_list;
    private Map<String, String> maker_list;
    private List<String> genre_list;
    private int position;

    public Video(String movieId, String poster, String score, String introduction, String movie_name, String tags,
                 List<String> actress_list, String release_year, String level, List<String> picture_list, Map<String,
            String> maker_list,int position, List<String> genre_list) {
        this.movieId = movieId;
        this.poster = poster;
        this.score = score;
        this.introduction = introduction;
        this.movie_name = movie_name;
        this.tags = tags;
        this.actress_list = actress_list;
        this.release_year = release_year;
        this.level = level;
        this.picture_list = picture_list;
        this.maker_list = maker_list;
        this.genre_list = genre_list;
        this.position = position;
    }

    public Video() {
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getMovie_name() {
        return movie_name;
    }

    public void setMovie_name(String movie_name) {
        this.movie_name = movie_name;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<String> getActress_list() {
        return actress_list;
    }

    public void setActress_list(List<String> actress_list) {
        this.actress_list = actress_list;
    }

    public String getRelease_year() {
        return release_year;
    }

    public void setRelease_year(String release_year) {
        this.release_year = release_year;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<String> getPicture_list() {
        return picture_list;
    }

    public void setPicture_list(List<String> picture_list) {
        this.picture_list = picture_list;
    }

    public Map<String, String> getMaker_list() {
        return maker_list;
    }

    public void setMaker_list(Map<String, String> maker_list) {
        this.maker_list = maker_list;
    }

    public List<String> getGenre_list() {
        return genre_list;
    }

    public void setGenre_list(List<String> genre_list) {
        this.genre_list = genre_list;
    }
}
