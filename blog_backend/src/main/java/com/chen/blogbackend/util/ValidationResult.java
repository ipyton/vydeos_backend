package com.chen.blogbackend.util;

public class ValidationResult {
    public String result = "";
    public int code = 0;

    public ValidationResult(String result, int code) {
        this.result = result;
        this.code = code;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
