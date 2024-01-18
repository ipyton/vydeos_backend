package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

@Entity
public class UnfinishedUpload {
    private String ownerName = "";
    private String fileHash = "";
    private int total;
    private int current;

    public UnfinishedUpload(String ownerName, String fileHash, int total, int current, int startTime) {
        this.ownerName = ownerName;
        this.fileHash = fileHash;
        this.total = total;
        this.current = current;
        this.startTime = startTime;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    private int startTime;
}
