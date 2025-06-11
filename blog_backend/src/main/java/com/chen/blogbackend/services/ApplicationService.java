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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    @Autowired
    AccountService accountService;

    @Autowired
    SettingsService settingsService;

    @Autowired
    CqlSession session;

    PreparedStatement getApplicationsSimple;
    PreparedStatement getDetailedIntroduction;
    PreparedStatement saveApplication;
    PreparedStatement getInstalledApps;
    PreparedStatement deleteApp;
    PreparedStatement saveComment;

    @PostConstruct
    public void init() {
        logger.info("Initializing ApplicationService and preparing database statements");

        try {
            getApplicationsSimple = session.prepare("select applicationId, name, ratings, pictures, author from apps.application;");
            getDetailedIntroduction = session.prepare("select * from apps.application where applicationId=?;");
            getInstalledApps = session.prepare("select * from apps.user_apps where userId=?;");
            deleteApp = session.prepare("delete from apps.user_apps where userId = ? and applicationId = ?");
            saveComment = session.prepare("insert into apps.app_comment (appId, commentId , userId , userName, userAvatar, comment , rate, commentDateTime) values (?,?,?,?,?,?,?,?)");
            saveApplication = session.prepare("insert into apps.application (applicationid, version," +
                    " author, history_versions, hot_comments,introduction, lastmodified, name, pictures, ratings, system_requirements, type) values (?,?,?,?,?,?,?,?,?,?,?,?)");

            logger.info("Successfully initialized all prepared statements for ApplicationService");
        } catch (Exception e) {
            logger.error("Failed to initialize ApplicationService prepared statements", e);
            throw new RuntimeException("ApplicationService initialization failed", e);
        }
    }

    public PagingMessage<App> getBatchApps(PagingState pagingState, PreparedStatement statement) {
        logger.debug("Getting batch of apps with paging state: {}", pagingState != null ? "present" : "null");

        try {
            ResultSet execute;
            if (null != pagingState) {
                execute = session.execute(statement.bind().setPagingState(pagingState).setPageSize(10));
                logger.debug("Executing query with existing paging state, page size: 10");
            } else {
                execute = session.execute(statement.bind().setPageSize(5));
                logger.debug("Executing query with initial page size: 5");
            }

            List<App> convert = ApplicationParser.convertToApp(execute);
            logger.debug("Converted {} apps from result set", convert.size());

            PagingMessage<App> message = new PagingMessage<>();
            message.items = convert;
            PagingState safePagingState = execute.getExecutionInfo().getSafePagingState();

            if (safePagingState != null) {
                message.pagingInformation = safePagingState.toString();
                logger.debug("Set paging information for next page: {}", message.pagingInformation);
            } else {
                logger.debug("No more pages available");
            }

            return message;
        } catch (Exception e) {
            logger.error("Error getting batch of apps", e);
            // Return empty message on error
            PagingMessage<App> errorMessage = new PagingMessage<>();
            errorMessage.items = List.of();
            return errorMessage;
        }
    }

    public PagingMessage<App> getPagingApplications(PagingState state) {
        logger.debug("Getting paging applications with detailed information");
        try {
            PagingMessage<App> result = getBatchApps(state, getDetailedIntroduction);
            logger.info("Successfully retrieved {} applications with detailed information", result.items.size());
            return result;
        } catch (Exception e) {
            logger.error("Error getting paging applications", e);
            throw e;
        }
    }

    public PagingMessage<App> getPagingIntroductions(PagingState state) {
        logger.debug("Getting paging application introductions");
        try {
            PagingMessage<App> result = getBatchApps(state, getApplicationsSimple);
            logger.info("Successfully retrieved {} application introductions", result.items.size());
            return result;
        } catch (Exception e) {
            logger.error("Error getting paging application introductions", e);
            throw e;
        }
    }

    public PagingMessage<App> getInstalledApps(String userId) {
        logger.debug("Getting installed apps for user ID: {}", userId);
        try {
            ResultSet execute = session.execute(getInstalledApps.bind(userId));
            PagingMessage<App> message = new PagingMessage<>();
            message.items = ApplicationParser.convertToApp(execute);
            logger.info("Successfully retrieved {} installed apps for user ID: {}", message.items.size(), userId);
            return message;
        } catch (Exception e) {
            logger.error("Error getting installed apps for user ID: {}", userId, e);
            // Return empty message on error
            PagingMessage<App> errorMessage = new PagingMessage<>();
            errorMessage.items = List.of();
            return errorMessage;
        }
    }

    public List<App> getApplicationDetailById(String applicationId) {
        logger.debug("Getting application details for application ID: {}", applicationId);
        try {
            ResultSet execute = session.execute(getDetailedIntroduction.bind(applicationId));
            List<App> apps = ApplicationParser.convertToAppDetail(execute);
            logger.info("Successfully retrieved {} application details for ID: {}", apps.size(), applicationId);
            return apps;
        } catch (Exception e) {
            logger.error("Error getting application details for ID: {}", applicationId, e);
            return List.of();
        }
    }

    public boolean installApplication(String userId, String applicationID, HashMap<String, String> environment) {
        logger.info("Installing application {} for user: {}", applicationID, userId);
        try {
            List<App> applicationDetails = getApplicationDetailById(applicationID);
            if (applicationDetails.isEmpty()) {
                logger.warn("No application details found for application ID: {}", applicationID);
                return false;
            }

            App applicationDetail = applicationDetails.get(0);
            boolean meetsRequirements = RequirementCheck.check(applicationDetail.getSystemRequirements(), environment);

            if (meetsRequirements) {
                logger.info("Successfully installed application {} for user: {}", applicationID, userId);
            } else {
                logger.warn("System requirements not met for application {} and user: {}", applicationID, userId);
            }

            return meetsRequirements;
        } catch (Exception e) {
            logger.error("Error installing application {} for user: {}", applicationID, userId, e);
            return false;
        }
    }

    public boolean uploadApplication(App app) {
        logger.info("Uploading application: {} (ID: {})", app.getAppName(), app.getAppId());
        try {
            session.execute(saveApplication.bind(app.getAppId(), app.getAppName(), app.getVersion(), app.getLastModified()));
            logger.info("Successfully uploaded application: {} (ID: {})", app.getAppName(), app.getAppId());
            return true;
        } catch (Exception e) {
            logger.error("Error uploading application: {} (ID: {})", app.getAppName(), app.getAppId(), e);
            return false;
        }
    }

    public boolean meetRequirements(HashMap<String, String> requirements, HashMap<String, String> environment) {
        logger.debug("Checking if environment meets requirements - placeholder method");
        logger.debug("Requirements: {}, Environment: {}", requirements.size(), environment.size());
        return true;
    }

    public boolean deleteApplication(String userId, String applicationId) {
        logger.info("Deleting application {} for user: {}", applicationId, userId);
        try {
            ResultSet result = session.execute(deleteApp.bind(userId, applicationId));
            boolean success = result.getExecutionInfo().getErrors().size() == 0;

            if (success) {
                logger.info("Successfully deleted application {} for user: {}", applicationId, userId);
            } else {
                logger.error("Failed to delete application {} for user: {}, errors: {}",
                        applicationId, userId, result.getExecutionInfo().getErrors());
            }

            return success;
        } catch (Exception e) {
            logger.error("Error deleting application {} for user: {}", applicationId, userId, e);
            return false;
        }
    }

    public boolean comment(ApplicationComment comment) {
        logger.info("Adding comment for application: {} by user: {}", comment.getApplicationId(), comment.getUserId());
        logger.debug("Comment details - ID: {}, Rate: {}, DateTime: {}",
                comment.getCommentId(), comment.getRate(), comment.getCommentDateTime());

        try {
            session.execute(saveComment.bind(comment.getApplicationId(), comment.getCommentId(),
                    comment.getUserId(), comment.getUserName(), comment.getUserAvatar(),
                    comment.getComment(), comment.getRate(), comment.getCommentDateTime()));

            logger.info("Successfully saved comment {} for application: {} by user: {}",
                    comment.getCommentId(), comment.getApplicationId(), comment.getUserId());
            return true;
        } catch (Exception e) {
            logger.error("Error saving comment {} for application: {} by user: {}",
                    comment.getCommentId(), comment.getApplicationId(), comment.getUserId(), e);
            return false;
        }
    }
}