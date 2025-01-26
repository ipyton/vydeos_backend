package com.chen.notification.entities;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

public class Endpoints {

    private long from = 0;
    private long to = 0;
    private ArrayList<String> endpoints;

    public Endpoints(long from, long to, ArrayList<String> endpoints) {
        this.from = from;
        this.to = to;
        this.endpoints = endpoints;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public ArrayList<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(ArrayList<String> endpoints) {
        this.endpoints = endpoints;
    }
}
