package com.chen.blogbackend.entities;

public class Trend {
    String id;
    String intro;
    String content;
    String pic;
    Double score;

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public Trend(String id, String intro, String content, String pic, Double score) {
        this.id = id;
        this.intro = intro;
        this.content = content;
        this.pic = pic;
        this.score = score;
    }

    public Trend(String id, String intro, String content, String pic) {
        this.id = id;
        this.intro = intro;
        this.content = content;
        this.pic = pic;
    }
}
