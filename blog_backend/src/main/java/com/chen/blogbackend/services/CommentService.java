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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

@Service
public class CommentService {
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
        try {
//            ApplicationCommentMapper build = new ApplicationCommentMapperBuilder(session).build();
//            applicationCommentDao = build.getDao();
//            CommentMapper commentMapper = new CommentMapperBuilder(session).build();
//            commentDao = commentMapper.getDao();


            getCommentsByResourceId = session.prepare("select * from comment.comments where resource_id=? and type=?;");
            sendComment = session.prepare("insert into comment.comments (resource_id,type,time,user_id,content,likes) values (?, ?, ?, ?, ?, ?);");
            getCommentsByUserId = session.prepare("select * from comment.comment_by_user_id where user_id=?");
//            addComment = session.prepare("insert into comment.comment_by_content values(?,?,?,?,?,?,?,?)");
            addCommentForApp = session.prepare("insert into comment.app_comment values(?, ?, ?, ?, ?, ?, ?, ?)");
            like = session.prepare("update comment.comments_by_content set likes = likes + 1 where object_id=?");
            deleteSubComment = session.prepare("delete from comment.comment_by_comment where comment_refer=?");
            deleteComment = session.prepare("delete from comment.comments where object_id=? and type = ? and comment_id=?");
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public List<Comment> getCommentByResourceId(String resourceId, String type, String pagingState) {
        if (resourceId == null || resourceId.isEmpty() || type == null || type.isEmpty()) {
            throw new IllegalArgumentException("resourceId cannot be null");
        }
        ResultSet execute = session.execute(getCommentsByResourceId.bind(resourceId, type));

        return CommentMapper.commentsMapper(execute);
    }

    public PagingMessage<Comment> getCommentByUserId(String userId, String pagingState) {
        return getCommentPagingMessage(userId, pagingState, getCommentsByUserId);
    }

    @NotNull
    private PagingMessage<Comment> getCommentPagingMessage(String userId, String pagingState, PreparedStatement getCommentsByUserId) {
        ResultSet execute = session.execute(getCommentsByUserId.bind(userId).setPagingState(PagingState.fromString(pagingState)));
        ByteBuffer newPagingState = execute.getExecutionInfo().getPagingState();
        PagingIterable<Comment> convert = commentDao.convert(execute);
        assert newPagingState != null;
        return new PagingMessage<>(convert.all(), newPagingState.toString(), 0);
    }

    @Deprecated
    public boolean addSubComment(String objectId, Comment comment) {
        return addComment(objectId, comment, true);

    }

    @Deprecated
    public boolean addCommentForContent(String objectId, Comment comment) {
        return addComment(objectId, comment, false);
    }

    public boolean addComment(String objectID, Comment comment, boolean refer) {
        ResultSet execute = session.execute(addComment.bind(objectID, comment.getCommentId(), Instant.now(),
                comment.getContent(), refer ? "true" : "false", comment.getUserName(), comment.getUserId(), comment.getAvatar()));
        return execute.getExecutionInfo().getErrors().size() == 0;

    }


    public boolean deleteComment(String objectId,String commentID, boolean refer) {
        ResultSet result = null;
        if (refer) {
            result = session.execute(deleteComment.bind(objectId, commentID));
        }
        else {
            result = session.execute(deleteSubComment.bind(objectId, commentID));
        }
        return result.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean like(String objectId, String commentID) {
        ResultSet execute = session.execute(getCommentsByCommentId.bind(commentID));
        Row one = execute.one();
        ResultSet result = session.execute(like.bind(objectId, commentID));

        return result.getExecutionInfo().getErrors().isEmpty();
    }


    public boolean addApplicationComment(ApplicationComment comment) {
        ResultSet execute = session.execute(addCommentForApp.bind(comment.getApplicationId(), comment.getCommentId(),
                comment.getUserId(), comment.getComment(), comment.getCommentDateTime(), comment.getRate(), comment.getUserAvatar(), comment.getUserName()));
        return execute.getExecutionInfo().getErrors().size() == 0;
    }


    public PagingMessage<ApplicationComment> getApplicationComment(String applicationId, String pagingState) {
        ResultSet execute = session.execute(getApplicationComments.bind(applicationId).setPagingState(PagingState.fromString(pagingState)));
        PagingIterable<ApplicationComment> convert = applicationCommentDao.convert(execute);
        return new PagingMessage<>(convert.all(), execute.getExecutionInfo().getPagingState().toString(),-1);
    }


    public PagingMessage<Comment> getCommentByCommentId(String commentId,String pagingState) {
        ResultSet execute = session.execute(getCommentsByCommentId.bind(commentId).setPagingState(PagingState.fromString(pagingState)));
        PagingIterable<Comment> convert = commentDao.convert(execute);
        return new PagingMessage<>(convert.all(), execute.getExecutionInfo().getPagingState().toString(),-1);

    }

    public boolean dislike(String objectId, String commentID) {
        ResultSet result = session.execute(like.bind(objectId, commentID));
        return result.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean sendComment(String userEmail, String content, String resourceId, String type, long likes) {
        ResultSet execute = session.execute(sendComment.bind(resourceId, type, Instant.now(), userEmail,
                content, likes));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }
}
