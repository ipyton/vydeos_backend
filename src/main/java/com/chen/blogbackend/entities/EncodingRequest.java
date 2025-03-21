package com.chen.blogbackend.entities;

public class EncodingRequest {
    public String inputPath;
    public String outputPath;
    public String inputSource;
    public String outputSource;
    public String resourceId;
    public String type;

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

    public EncodingRequest(String inputPath, String outputPath, String inputSource, String outputSource, String resourceId, String type) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.inputSource = inputSource;
        this.outputSource = outputSource;
        this.resourceId = resourceId;
        this.type = type;
    }

    public EncodingRequest(String inputPath, String outputPath, String inputSource, String outputSource) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.inputSource = inputSource;
        this.outputSource = outputSource;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getInputSource() {
        return inputSource;
    }

    public void setInputSource(String inputSource) {
        this.inputSource = inputSource;
    }

    public String getOutputSource() {
        return outputSource;
    }

    public void setOutputSource(String outputSource) {
        this.outputSource = outputSource;
    }
}
