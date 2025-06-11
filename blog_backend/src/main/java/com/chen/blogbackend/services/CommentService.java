package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.ApplicationCommentDao;
import com.chen.blogbackend.DAO.CommentDao;
import com.chen.blogbackend.entities.ApplicationComment;
import com.chen.blogbackend.entities.Comment;
import com.chen.blogbackend.mappers.CommentMapper;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PagingState;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    CqlSession session;

    PreparedStatement getCommentsByResourceId;
    PreparedStatement getCommentsByCommentId;
    PreparedStatement getCommentsByUserId;
    PreparedStatement getApplicationComments;

    PreparedStatement addComment;
    PreparedStatement addCommentForApp;

    PreparedStatement deleteComment;
    PreparedStatement deleteSubComment;

    PreparedStatement like;
    PreparedStatement dislike;
    PreparedStatement sendComment;
    ApplicationCommentDao applicationCommentDao;
    CommentDao commentDao;

    @PostConstruct
    public void init(){
        logger.info("Initializing CommentService and preparing CQL statements");
        try {
//            ApplicationCommentMapper build = new ApplicationCommentMapperBuilder(session).build();
//            applicationCommentDao = build.getDao();
//            CommentMapper commentMapper = new CommentMapperBuilder(session).build();
//            commentDao = commentMapper.getDao();

            getCommentsByResourceId = session.prepare("select * from comment.comments where resource_id=? and type=?;");
            sendComment = session.prepare("insert into comment.comments (resource_id,type,time,user_id,content,likes) values (?, ?, ?, ?, ?, ?);");
            //getCommentsByUserId = session.prepare("select * from comment.comments_by_user_id where user_id=?");
//            addComment = session.prepare("insert into comment.comments_by_content values(?,?,?,?,?,?,?,?)");
            //addCommentForApp = session.prepare("insert into comment.app_comment values(?, ?, ?, ?, ?, ?, ?, ?)");
            //like = session.prepare("update comment.comments_by_content set likes = likes + 1 where object_id=?");
            //deleteSubComment = session.prepare("delete from comment.comment_by_comment where comment_refer=?");
            //deleteComment = session.prepare("delete from comment.comments where object_id=? and type = ? and comment_id=?");

            logger.info("CommentService initialization completed successfully");
        }
        catch (Exception e) {
            logger.error("Failed to initialize CommentService: {}", e.getMessage(), e);
            throw new RuntimeException("CommentService initialization failed", e);
        }
    }

    public List<Comment> getCommentByResourceId(String resourceId, String type, String pagingState) {
        logger.debug("Getting comments by resourceId: {}, type: {}, pagingState: {}", resourceId, type, pagingState);

        if (resourceId == null || resourceId.isEmpty() || type == null || type.isEmpty()) {
            logger.warn("Invalid parameters - resourceId: {}, type: {}", resourceId, type);
            throw new IllegalArgumentException("resourceId and type cannot be null or empty");
        }

        try {
            ResultSet execute = session.execute(getCommentsByResourceId.bind(resourceId, type));
            List<Comment> comments = CommentMapper.commentsMapper(execute);
            logger.info("Successfully retrieved {} comments for resourceId: {}, type: {}", comments.size(), resourceId, type);
            return comments;
        } catch (Exception e) {
            logger.error("Failed to get comments by resourceId: {}, type: {} - {}", resourceId, type, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve comments", e);
        }
    }

    public PagingMessage<Comment> getCommentByUserId(String userId, String pagingState) {
        logger.debug("Getting comments by userId: {}, pagingState: {}", userId, pagingState);
        return getCommentPagingMessage(userId, pagingState, getCommentsByUserId);
    }

    @NotNull
    private PagingMessage<Comment> getCommentPagingMessage(String userId, String pagingState, PreparedStatement getCommentsByUserId) {
        logger.debug("Executing paging query for userId: {}", userId);

        try {
            ResultSet execute = session.execute(getCommentsByUserId.bind(userId).setPagingState(PagingState.fromString(pagingState)));
            ByteBuffer newPagingState = execute.getExecutionInfo().getPagingState();
            PagingIterable<Comment> convert = commentDao.convert(execute);
            assert newPagingState != null;

            PagingMessage<Comment> result = new PagingMessage<>(convert.all(), newPagingState.toString(), 0);
            logger.info("Successfully retrieved paged comments for userId: {}", userId);
            return result;
        } catch (Exception e) {
            logger.error("Failed to get paged comments for userId: {} - {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve paged comments", e);
        }
    }

    @Deprecated
    public boolean addSubComment(String objectId, Comment comment) {
        logger.debug("Adding sub comment for objectId: {}", objectId);
        return addComment(objectId, comment, true);
    }

    @Deprecated
    public boolean addCommentForContent(String objectId, Comment comment) {
        logger.debug("Adding comment for content objectId: {}", objectId);
        return addComment(objectId, comment, false);
    }

    public boolean addComment(String objectID, Comment comment, boolean refer) {
        logger.debug("Adding comment - objectID: {}, commentId: {}, refer: {}", objectID, comment.getCommentId(), refer);

        try {
            ResultSet execute = session.execute(addComment.bind(objectID, comment.getCommentId(), Instant.now(),
                    comment.getContent(), refer ? "true" : "false", comment.getUserName(), comment.getUserId(), comment.getAvatar()));
            boolean success = execute.getExecutionInfo().getErrors().size() == 0;

            if (success) {
                logger.info("Successfully added comment - objectID: {}, commentId: {}", objectID, comment.getCommentId());
            } else {
                logger.warn("Failed to add comment - objectID: {}, commentId: {}, errors: {}",
                        objectID, comment.getCommentId(), execute.getExecutionInfo().getErrors());
            }

            return success;
        } catch (Exception e) {
            logger.error("Exception while adding comment - objectID: {}, commentId: {} - {}",
                    objectID, comment.getCommentId(), e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteComment(String objectId, String commentID, boolean refer) {
        logger.debug("Deleting comment - objectId: {}, commentID: {}, refer: {}", objectId, commentID, refer);

        try {
            ResultSet result = null;
            if (refer) {
                result = session.execute(deleteComment.bind(objectId, commentID));
            } else {
                result = session.execute(deleteSubComment.bind(objectId, commentID));
            }

            boolean success = result.getExecutionInfo().getErrors().isEmpty();

            if (success) {
                logger.info("Successfully deleted comment - objectId: {}, commentID: {}", objectId, commentID);
            } else {
                logger.warn("Failed to delete comment - objectId: {}, commentID: {}, errors: {}",
                        objectId, commentID, result.getExecutionInfo().getErrors());
            }

            return success;
        } catch (Exception e) {
            logger.error("Exception while deleting comment - objectId: {}, commentID: {} - {}",
                    objectId, commentID, e.getMessage(), e);
            return false;
        }
    }

    public boolean like(String objectId, String commentID) {
        logger.debug("Liking comment - objectId: {}, commentID: {}", objectId, commentID);

        try {
            ResultSet execute = session.execute(getCommentsByCommentId.bind(commentID));
            Row one = execute.one();
            ResultSet result = session.execute(like.bind(objectId, commentID));

            boolean success = result.getExecutionInfo().getErrors().isEmpty();

            if (success) {
                logger.info("Successfully liked comment - objectId: {}, commentID: {}", objectId, commentID);
            } else {
                logger.warn("Failed to like comment - objectId: {}, commentID: {}, errors: {}",
                        objectId, commentID, result.getExecutionInfo().getErrors());
            }

            return success;
        } catch (Exception e) {
            logger.error("Exception while liking comment - objectId: {}, commentID: {} - {}",
                    objectId, commentID, e.getMessage(), e);
            return false;
        }
    }

    public boolean addApplicationComment(ApplicationComment comment) {
        logger.debug("Adding application comment - applicationId: {}, commentId: {}",
                comment.getApplicationId(), comment.getCommentId());

        try {
            ResultSet execute = session.execute(addCommentForApp.bind(comment.getApplicationId(), comment.getCommentId(),
                    comment.getUserId(), comment.getComment(), comment.getCommentDateTime(), comment.getRate(),
                    comment.getUserAvatar(), comment.getUserName()));

            boolean success = execute.getExecutionInfo().getErrors().size() == 0;

            if (success) {
                logger.info("Successfully added application comment - applicationId: {}, commentId: {}",
                        comment.getApplicationId(), comment.getCommentId());
            } else {
                logger.warn("Failed to add application comment - applicationId: {}, commentId: {}, errors: {}",
                        comment.getApplicationId(), comment.getCommentId(), execute.getExecutionInfo().getErrors());
            }

            return success;
        } catch (Exception e) {
            logger.error("Exception while adding application comment - applicationId: {}, commentId: {} - {}",
                    comment.getApplicationId(), comment.getCommentId(), e.getMessage(), e);
            return false;
        }
    }

    public PagingMessage<ApplicationComment> getApplicationComment(String applicationId, String pagingState) {
        logger.debug("Getting application comments - applicationId: {}, pagingState: {}", applicationId, pagingState);

        try {
            ResultSet execute = session.execute(getApplicationComments.bind(applicationId).setPagingState(PagingState.fromString(pagingState)));
            PagingIterable<ApplicationComment> convert = applicationCommentDao.convert(execute);

            PagingMessage<ApplicationComment> result = new PagingMessage<>(convert.all(),
                    execute.getExecutionInfo().getPagingState().toString(), -1);

            logger.info("Successfully retrieved application comments for applicationId: {}", applicationId);
            return result;
        } catch (Exception e) {
            logger.error("Failed to get application comments for applicationId: {} - {}",
                    applicationId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve application comments", e);
        }
    }

    public PagingMessage<Comment> getCommentByCommentId(String commentId, String pagingState) {
        logger.debug("Getting comments by commentId: {}, pagingState: {}", commentId, pagingState);

        try {
            ResultSet execute = session.execute(getCommentsByCommentId.bind(commentId).setPagingState(PagingState.fromString(pagingState)));
            PagingIterable<Comment> convert = commentDao.convert(execute);

            PagingMessage<Comment> result = new PagingMessage<>(convert.all(),
                    execute.getExecutionInfo().getPagingState().toString(), -1);

            logger.info("Successfully retrieved comments by commentId: {}", commentId);
            return result;
        } catch (Exception e) {
            logger.error("Failed to get comments by commentId: {} - {}", commentId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve comments by commentId", e);
        }
    }

    public boolean dislike(String objectId, String commentID) {
        logger.debug("Disliking comment - objectId: {}, commentID: {}", objectId, commentID);

        try {
            ResultSet result = session.execute(like.bind(objectId, commentID));
            boolean success = result.getExecutionInfo().getErrors().isEmpty();

            if (success) {
                logger.info("Successfully disliked comment - objectId: {}, commentID: {}", objectId, commentID);
            } else {
                logger.warn("Failed to dislike comment - objectId: {}, commentID: {}, errors: {}",
                        objectId, commentID, result.getExecutionInfo().getErrors());
            }

            return success;
        } catch (Exception e) {
            logger.error("Exception while disliking comment - objectId: {}, commentID: {} - {}",
                    objectId, commentID, e.getMessage(), e);
            return false;
        }
    }

    public boolean sendComment(String userEmail, String content, String resourceId, String type, long likes) {
        logger.debug("Sending comment - userEmail: {}, resourceId: {}, type: {}", userEmail, resourceId, type);

        try {
            ResultSet execute = session.execute(sendComment.bind(resourceId, type, Instant.now(), userEmail,
                    content, likes));

            boolean success = execute.getExecutionInfo().getErrors().isEmpty();

            if (success) {
                logger.info("Successfully sent comment - userEmail: {}, resourceId: {}, type: {}",
                        userEmail, resourceId, type);
            } else {
                logger.warn("Failed to send comment - userEmail: {}, resourceId: {}, type: {}, errors: {}",
                        userEmail, resourceId, type, execute.getExecutionInfo().getErrors());
            }

            return success;
        } catch (Exception e) {
            logger.error("Exception while sending comment - userEmail: {}, resourceId: {}, type: {} - {}",
                    userEmail, resourceId, type, e.getMessage(), e);
            return false;
        }
    }
}