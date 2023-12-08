package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.App;

import java.util.ArrayList;

public class ApplicationService {


    public ArrayList<App> searchByName(String name){
        return new ArrayList<>();
    }

    public ArrayList<App> searchByDescription(String description){


        return new ArrayList<>();
    }

    public boolean installApplication(String userId, String applicationID) {
        return true;
    }

    public boolean comment(String userId, String applicationId,String comment, int rate) {
        return true;
    }


}
