package com.chen.blogbackend.responseMessage;

import com.datastax.oss.driver.api.core.cql.PagingState;

import java.util.ArrayList;
import java.util.List;

public class PagingMessage<T> {
    public List<T> items;
    public String pagingInformation;
    public int code;
    public String ext="";

    public PagingMessage(List<T> items, String pagingInformation, int code, String ext) {
        this.items = items;
        this.pagingInformation = pagingInformation;
        this.code = code;
        this.ext = ext;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }

    public PagingMessage(List<T> items, String pagingInformation, int code) {
        this.items = items;
        this.pagingInformation = pagingInformation;
        this.code = code;
    }

    public PagingMessage() {
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(ArrayList<T> items) {
        this.items = items;
    }

    public String getPagingInformation() {
        return pagingInformation;
    }

    public void setPagingInformation(String pagingInformation) {
        this.pagingInformation = pagingInformation;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
