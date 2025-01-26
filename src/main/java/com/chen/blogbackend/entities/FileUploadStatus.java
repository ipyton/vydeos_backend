package com.chen.blogbackend.entities;

public class FileUploadStatus {
    public String userEmail;
    public String fileName;
    public String wholeHash;
    public String resourceType;

    public Integer totalSlices;
    public Integer currentSlice;
    public Long resourceId; //primary key
    // status:0 还没开始上传 status:1, 正在上传. status:2 正在处理，status:3 已上传
    public Integer statusCode;


    public FileUploadStatus() {
    }

    public FileUploadStatus(String userEmail, String fileName, Long resourceId, String resourceType, String wholeHash, Integer totalSlices, Integer currentSlice) {
        this.userEmail = userEmail;
        this.fileName = fileName;
        this.resourceId = resourceId;
        this.wholeHash = wholeHash;
        this.totalSlices = totalSlices;
        this.currentSlice = currentSlice;
        this.resourceType = resourceType;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long movieId) {
        this.resourceId = movieId;
    }

    public String getWholeHash() {
        return wholeHash;
    }

    public void setWholeHash(String wholeHash) {
        this.wholeHash = wholeHash;
    }

    public Integer getTotalSlices() {
        return totalSlices;
    }

    public void setTotalSlices(Integer totalSlices) {
        this.totalSlices = totalSlices;
    }

    public Integer getCurrentSlice() {
        return currentSlice;
    }

    public void setCurrentSlice(Integer currentSlice) {
        this.currentSlice = currentSlice;
    }
}
