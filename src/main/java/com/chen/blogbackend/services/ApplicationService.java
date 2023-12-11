package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.AppDao;
import com.chen.blogbackend.DAO.ApplicationCommentDao;
import com.chen.blogbackend.ResponseMessage.PagingMessage;
import com.chen.blogbackend.entities.App;
import com.chen.blogbackend.entities.ApplicationComment;
import com.chen.blogbackend.entities.Comment.Comment;
import com.chen.blogbackend.entities.Setting;
import com.chen.blogbackend.mappers.AppMapper;
import com.chen.blogbackend.mappers.AppMapperBuilder;
import com.chen.blogbackend.mappers.ApplicationCommentMapper;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PagingState;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class ApplicationService {

    @Autowired
    AccountService accountService;

    @Autowired
    SettingsService settingsService;

    @Autowired
    CqlSession session;

    AppDao appDao;
    ApplicationCommentDao commentDao;

    PreparedStatement getApplications;
    PreparedStatement getComments;
    PreparedStatement getSimpleIntroduction;
    PreparedStatement setComment;
    PreparedStatement setApplication;


    @PostConstruct
    public void init(){
        AppMapper appMapper = new AppMapperBuilder(session).build();
        appDao = appMapper.appDao();
        //generates here


        getApplications = session.prepare("");
        getComments = session.prepare("");
        getSimpleIntroduction = session.prepare("");
        setComment = session.prepare("");
        setApplication = session.prepare("");




    }


    public PagingMessage<App> getPagingApplications(String userId, String pagingState) {

        return new PagingMessage<>();

    }

    public ArrayList<Comment> getPagingComments(String userId, String applicationId) {
        return new ArrayList<>();
    }

    public App getApplicationDetailById(String applicationId ){
        return new App();

    }


    public boolean installApplication(String userId, String applicationID) {

        return true;
    }

    public boolean comment(String userId, String applicationId,String comment, int rate) {
        return true;
    }

    public boolean uploadApplication(){
        return true;

    }
    public boolean meetRequirements(HashMap<String, String> requirements, HashMap<String, String> environment) {
        return true;
    }


}
