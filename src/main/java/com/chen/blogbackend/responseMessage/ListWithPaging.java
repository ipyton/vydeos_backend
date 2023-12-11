package com.chen.blogbackend.ResponseMessage;

import com.datastax.oss.driver.api.core.cql.PagingState;

import java.util.ArrayList;

public class ListWithPaging<T> {
    ArrayList<T> arrayList;
    PagingState state;

    public ListWithPaging(ArrayList<T> arrayList, PagingState state) {
        this.arrayList = arrayList;
        this.state = state;
    }

    public ArrayList<T> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<T> arrayList) {
        this.arrayList = arrayList;
    }

    public PagingState getState() {
        return state;
    }

    public void setState(PagingState state) {
        this.state = state;
    }
}
