package com.chen.blogbackend.entities;

public class VideoSpecifications {
    private int width = 0;
    private int height = 0;
    private String samplingFormat;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getSamplingFormat() {
        return samplingFormat;
    }

    public void setSamplingFormat(String samplingFormat) {
        this.samplingFormat = samplingFormat;
    }

    public VideoSpecifications(int width, int height, String samplingFormat) {
        this.width = width;
        this.height = height;
        this.samplingFormat = samplingFormat;
    }
}
