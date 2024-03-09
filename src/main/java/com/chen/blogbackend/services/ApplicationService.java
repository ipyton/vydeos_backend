package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.AppDao;
import com.chen.blogbackend.DAO.CommentDao;
import com.chen.blogbackend.entities.Comment;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.entities.App;
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

import java.util.HashMap;
import java.util.Objects;

@Service
public class ApplicationService {

    @Autowired
    AccountService accountService;

    @Autowired
    SettingsService settingsService;

    @Autowired
    CqlSession session;

    AppDao appDao;
    CommentDao commentDao;

    PreparedStatement getApplications;
    PreparedStatement getSimpleIntroduction;
    PreparedStatement setApplication;
    PreparedStatement getInstalledApps;
    PreparedStatement deleteApp;

    @PostConstruct
    public void init(){
        session.execute("use apps");
//        AppMapper appMapper = new AppMapperBuilder(session).build();
//        appDao = appMapper.appDao();
//        getApplications = session.prepare("select * from application;");
//        getSimpleIntroduction = session.prepare("select * from simple_application_intro where applicationId=?;");
//        getInstalledApps = session.prepare("select * from user_apps where userId=?;");
//        deleteApp = session.prepare("delete from user_apps where userId = ? and applicationId = ?");
    }

    public PagingMessage<App> getBatchApps(PagingState pagingState, PreparedStatement statement) {
        ResultSet execute;
        if (null != pagingState) {
            execute = session.execute(getApplications.bind().setPagingState(pagingState));
        }
        else {
            execute = session.execute(getApplications.bind());
        }
        PagingIterable<App> convert = appDao.convert(execute);

        PagingMessage<App> message = new PagingMessage<>();
        message.items = convert.all();
        message.pagingInformation = Objects.requireNonNull(execute.getExecutionInfo().getSafePagingState()).toString();
        return message;
    }

    public PagingMessage<App> getPagingApplications(PagingState state) {
        return getBatchApps(state, getApplications);
    }

    public PagingMessage<App> getPagingIntroductions(PagingState state) {
        return getBatchApps(state, getSimpleIntroduction);
    }

    public PagingMessage<App> getInstalledApps(String userId) {
        ResultSet execute = session.execute(getInstalledApps.bind(userId));
        PagingMessage<App> message = new PagingMessage<>();
        message.items = appDao.convert(execute).all();
        return message;
    }

    public App getApplicationDetailById(String applicationId){
        return appDao.getAppDetails(applicationId);
    }

    public boolean installApplication(String userId, String applicationID, HashMap<String, String> environment) {
        App applicationDetailById = getApplicationDetailById(applicationID);
        return RequirementCheck.check(applicationDetailById.getSystemRequirements(), environment);
    }

    public boolean uploadApplication(App app){
        appDao.insert(app);
        return true;
    }

    public boolean meetRequirements(HashMap<String, String> requirements, HashMap<String, String> environment) {
        return true;
    }

    public boolean deleteApplication(String userId, String applicationId) {
        return session.execute(deleteApp.bind(userId, applicationId)).getExecutionInfo().getErrors().size() == 0;
    }

    public boolean comment(Comment comment) {
        commentDao.save(comment);
        return true;
    }



}
