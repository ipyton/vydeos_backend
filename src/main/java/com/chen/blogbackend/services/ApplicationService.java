package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.AppDao;
import com.chen.blogbackend.DAO.ApplicationCommentDao;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.entities.App;
import com.chen.blogbackend.entities.Comment;
import com.chen.blogbackend.mappers.AppMapper;
import com.chen.blogbackend.mappers.AppMapperBuilder;
import com.chen.blogbackend.util.RequirementCheck;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PagingState;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import jakarta.annotation.PostConstruct;
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

    PreparedStatement getApplications;
    PreparedStatement getSimpleIntroduction;
    PreparedStatement setApplication;

    @PostConstruct
    public void init(){
        AppMapper appMapper = new AppMapperBuilder(session).build();
        appDao = appMapper.appDao();
        getApplications = session.prepare("");
        getSimpleIntroduction = session.prepare("");

    }

    public PagingMessage<App> getPagingApplications(String userId, PagingState pagingState) {
        ResultSet execute = null;
        if (null != pagingState) {
            execute = session.execute(getApplications.bind(userId).setPagingState(pagingState));
        }
        else {
            execute = session.execute(getApplications.bind(userId));
        }
        PagingIterable<App> convert = appDao.convert(execute);

        PagingMessage<App> message = new PagingMessage<>();
        message.items = convert.all();
        message.pagingInformation = execute.getExecutionInfo().getSafePagingState();
        return new PagingMessage<>();
    }


    public App getApplicationDetailById(String applicationId ){
        return appDao.getAppDetails(applicationId);
    }

    public boolean installApplication(String userId, String applicationID, HashMap<String, String> environment) {
        App applicationDetailById = getApplicationDetailById(applicationID);
        if (RequirementCheck.check(applicationDetailById.getSystemRequirements(), environment)) {
            return true;
        }
        return false;
    }

    public boolean uploadApplication(App app){
        appDao.insert(app);
        return true;
    }
    public boolean meetRequirements(HashMap<String, String> requirements, HashMap<String, String> environment) {
        return true;
    }


}
