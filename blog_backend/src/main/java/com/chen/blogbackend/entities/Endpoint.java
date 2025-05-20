package com.chen.blogbackend.entities;


public class Endpoint {
    public long start;
    public long end;
    public String address;
    public String type;
    public long id;
    public Endpoint() {

    }

    public Endpoint(long start, long end, String address, long id) {
        this.start = start;
        this.end = end;
        this.address = address;
        this.id = id;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
