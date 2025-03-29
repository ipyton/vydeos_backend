package com.chen.blogbackend.entities;


public class Playable  {

    private String resourceId;

    private String type;

    private Byte quality;  // `tinyint` in SQL maps to `Byte` in Java

    private String bucket;
    private String path;

    public Playable() {}

    public Playable(String resourceId, String type, Byte quality, String bucket, String path) {
        this.resourceId = resourceId;
        this.type = type;
        this.quality = quality;
        this.bucket = bucket;
        this.path = path;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Byte getQuality() {
        return quality;
    }

    public void setQuality(Byte quality) {
        this.quality = quality;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Video{" +
                "resourceId='" + resourceId + '\'' +
                ", type='" + type + '\'' +
                ", quality=" + quality +
                ", bucket='" + bucket + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}

