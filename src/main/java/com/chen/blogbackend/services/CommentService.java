package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.ApplicationCommentDao;
import com.chen.blogbackend.DAO.CommentDao;
import com.chen.blogbackend.entities.App;
import com.chen.blogbackend.entities.ApplicationComment;
import com.chen.blogbackend.entities.Comment;
import com.chen.blogbackend.mappers.ApplicationCommentMapper;
import com.chen.blogbackend.mappers.ApplicationCommentMapperBuilder;
import com.chen.blogbackend.mappers.CommentMapper;
import com.chen.blogbackend.mappers.CommentMapperBuilder;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.util.PagingStateConverter;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    CqlSession session;

    PreparedStatement getCommentsByObjectId;
    PreparedStatement getCommentsByCommentId;
    PreparedStatement getCommentsByUserId;
    PreparedStatement getApplicationComments;

    PreparedStatement addCommentForComment;
    PreparedStatement addCommentForApp;

    PreparedStatement deleteComment;
    PreparedStatement deleteSubComment;

    PreparedStatement like;
    PreparedStatement likeSub;

    ApplicationCommentDao applicationCommentDao;
    CommentDao commentDao;

    @PostConstruct
    public void init(){
        ApplicationCommentMapper build = new ApplicationCommentMapperBuilder(session).build();
        applicationCommentDao = build.getDao();
        CommentMapper commentMapper = new CommentMapperBuilder(session).build();
        commentDao = commentMapper.getDao();

        getCommentsByObjectId = session.prepare("select * from comment_by_object_id where object_id=?");
        getCommentsByUserId = session.prepare("select * from comment_by_user_id where user_id=?");
        addCommentForComment = session.prepare("");
        addCommentForApp = session.prepare("insert into app_comment values(?,?,?,?)");
        like = session.prepare("update comments_by_comment set likes = likes + 1 where object_id=?");
        likeSub = session.prepare("update comments_by_object_id set likes = likes + 1 where comment_refer=?");
        deleteSubComment = session.prepare("delete from comment_by_comment where comment_refer=?");
        deleteComment = session.prepare("delete from comment_by_object_id where object_id=? and comment_id=?");
    }

    public PagingMessage<Comment> getCommentByObjectId(String objectId, String pagingState) {
        return getCommentPagingMessage(objectId, pagingState, getCommentsByObjectId);
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
    public boolean addSubComment(Comment comment) {


        return true;
    }

    public boolean addCommentForContent(Comment comment) {
        session.execute(addCommentForComment.bind())
        return true;
    }


    public boolean deleteComment(String objectId,String commentID, boolean refer) {
        ResultSet result = null;
        if (refer) {
            result = session.execute(deleteComment.bind(objectId, commentID));
        }
        else {
            result = session.execute(deleteSubComment.bind(objectId, commentID));
        }
        return result.getExecutionInfo().getErrors().size() == 0;
    }

    public boolean like(String commentID, boolean refer) {
        ResultSet execute = session.execute(getCommentsByCommentId.bind(commentID));
        Row one = execute.one();
        ResultSet result = null;
        if (refer) {
            assert one != null;
            result = session.execute(likeSub.bind(one.get("comment_refer", String.class)));
        }
        else {
            result = session.execute(like.bind(one.get("object_id", String.class)));
        }
        return result.getExecutionInfo().getErrors().size() == 0;
    }


    public boolean addApplicationComment(ApplicationComment comment) {
        ResultSet execute = session.execute(addCommentForApp.bind(comment.getApplicationId(), comment.getUserId(), comment.getComment()
                , comment.getRate(), comment.getPicture()));
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
}
