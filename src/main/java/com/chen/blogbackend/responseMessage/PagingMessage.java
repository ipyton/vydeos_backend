package com.chen.blogbackend.responseMessage;

import java.util.ArrayList;

public class PagingMessage<T> {
    public ArrayList<T> items;
    public String pagingInformation;
    public int code;

    public PagingMessage(ArrayList<T> items, String pagingInformation, int code) {
        this.items = items;
        this.pagingInformation = pagingInformation;
        this.code = code;
    }

    public PagingMessage() {
    }

    public ArrayList<T> getItems() {
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
