package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.ApplicationComment;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.entities.App;
import com.chen.blogbackend.util.ApplicationParser;
import com.chen.blogbackend.util.RequirementCheck;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PagingState;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    AccountService accountService;

    @Autowired
    SettingsService settingsService;

    @Autowired
    CqlSession session;

    //AppDao appDao;
    // CommentDao commentDao;

    PreparedStatement getApplicationsSimple;
    PreparedStatement getDetailedIntroduction;
    PreparedStatement saveApplication;
    PreparedStatement getInstalledApps;
    PreparedStatement deleteApp;
    PreparedStatement saveComment;

    @PostConstruct
    public void init(){
//        AppMapper appMapper = new AppMapperBuilder(session).withDefaultKeyspace("apps").build();
//        appDao = appMapper.appDao();
        getApplicationsSimple = session.prepare("select applicationId, name, ratings, pictures, author from apps.application;");
        getDetailedIntroduction = session.prepare("select * from apps.application where applicationId=?;");
        getInstalledApps = session.prepare("select * from apps.user_apps where userId=?;");
        deleteApp = session.prepare("delete from apps.user_apps where userId = ? and applicationId = ?");
        saveComment = session.prepare("insert into apps.app_comment (appId, commentId , userId , userName, userAvatar, comment , rate, commentDateTime) values (?,?,?,?,?,?,?,?)");
        saveApplication = session.prepare("insert into apps.application (applicationid, version," +
                " author, history_versions, hot_comments,introduction, lastmodified, name, pictures, ratings, system_requirements, type) values (?,?,?,?,?,?,?,?,?,?,?,?)");

    }

    public PagingMessage<App> getBatchApps(PagingState pagingState, PreparedStatement statement) {
        ResultSet execute;
        if (null != pagingState) {
            execute = session.execute(statement.bind().setPagingState(pagingState).setPageSize(10));
        }
        else {
            execute = session.execute(statement.bind().setPageSize(5));
        }
        List<App> convert = ApplicationParser.convertToApp(execute);

        PagingMessage<App> message = new PagingMessage<>();
        message.items = convert;
        PagingState safePagingState = execute.getExecutionInfo().getSafePagingState();
        if (safePagingState != null) {
            message.pagingInformation = safePagingState.toString();
            System.out.println(message.pagingInformation);
            return message;
        }
        return message;
    }

    public PagingMessage<App> getPagingApplications(PagingState state) {
        return getBatchApps(state, getDetailedIntroduction);
    }

    public PagingMessage<App> getPagingIntroductions(PagingState state) {
        return getBatchApps(state, getApplicationsSimple);
    }

    public PagingMessage<App> getInstalledApps(String userId) {
        ResultSet execute = session.execute(getInstalledApps.bind(userId));
        PagingMessage<App> message = new PagingMessage<>();
        message.items = ApplicationParser.convertToApp(execute);
        return message;
    }

    public List<App> getApplicationDetailById(String applicationId){
        ResultSet execute = session.execute(getDetailedIntroduction.bind(applicationId));
        return ApplicationParser.convertToAppDetail(execute);
    }

    public boolean installApplication(String userId, String applicationID, HashMap<String, String> environment) {
        List<App> applicationDetails= getApplicationDetailById(applicationID);
        App applicationDetail = applicationDetails.get(0);
        return RequirementCheck.check(applicationDetail.getSystemRequirements(), environment);
    }

    public boolean uploadApplication(App app){
        session.execute(saveApplication.bind(app.getAppId(), app.getAppName(), app.getVersion(), app.getLastModified()));
        return true;
    }

    public boolean meetRequirements(HashMap<String, String> requirements, HashMap<String, String> environment) {
        return true;
    }

    public boolean deleteApplication(String userId, String applicationId) {
        return session.execute(deleteApp.bind(userId, applicationId)).getExecutionInfo().getErrors().size() == 0;
    }

    public boolean comment(ApplicationComment comment) {
        System.out.println(comment.getCommentDateTime());
        try {
            session.execute(saveComment.bind(comment.getApplicationId(), comment.getCommentId(),
                    comment.getUserId(), comment.getUserName(), comment.getUserAvatar(), comment.getComment(), comment.getRate(), comment.getCommentDateTime()));
        } catch (Exception e) {
            return false;
        }
        return true;
    }



}
