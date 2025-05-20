package com.chen.blogbackend.entities;

public class SearchResult {
    public String resultType = "";
    public String resultName = "";
    public String resultDescription = "";
    public String picAddress = "";

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getResultName() {
        return resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = resultName;
    }

    public String getResultDescription() {
        return resultDescription;
    }

    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }

    public String getPicAddress() {
        return picAddress;
    }

    public void setPicAddress(String picAddress) {
        this.picAddress = picAddress;
    }

    public SearchResult() {
    }

    public SearchResult(String resultType, String resultName, String resultDescription, String picAddress) {
        this.resultType = resultType;
        this.resultName = resultName;
        this.resultDescription = resultDescription;
        this.picAddress = picAddress;
    }
}
