package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.List;

@Entity
public class Video {
    private String title;
    private List<String> type;
    private double rate;
    private String introductions;
    private List<String> images;
    private List<Cast> casts;

    public Video(String title, List<String> type, double rate, String introductions, List<String> images, List<Cast> casts) {
        this.title = title;
        this.type = type;
        this.rate = rate;
        this.introductions = introductions;
        this.images = images;
        this.casts = casts;
    }

    public Video() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getIntroductions() {
        return introductions;
    }

    public void setIntroductions(String introductions) {
        this.introductions = introductions;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<Cast> getCasts() {
        return casts;
    }

    public void setCasts(List<Cast> casts) {
        this.casts = casts;
    }
}
