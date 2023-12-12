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
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    CqlSession session;

    PreparedStatement getCommentsByObjectId;
    PreparedStatement getCommentsByUserId;
    PreparedStatement getApplicationComments;


    ApplicationCommentDao applicationCommentDao;
    CommentDao commentDao;

    @PostConstruct
    public void init(){
        ApplicationCommentMapper build = new ApplicationCommentMapperBuilder(session).build();
        applicationCommentDao = build.getDao();
        CommentMapper commentMapper = new CommentMapperBuilder(session).build();
        commentDao = commentMapper.getDao();

        getCommentsByObjectId = session.prepare("");
        getCommentsByUserId = session.prepare("");
    }

    public Comment getCommentByObjectId(String objectId) {
        return new Comment();
    }

    public Comment getCommentByUserId(String userId) {
        return new Comment();
    }

    public boolean addCommentByObjectId(String objectName) {
        return true;
    }

    public boolean addCommentByCommentId(String comment,String content) {
        return false;
    }

    public boolean deleteComment(String commentID) {
        return true;
    }

    public boolean like(String commentID) {

        return true;
    }

    public boolean addApplicationComment(ApplicationComment comment) {
        applicationCommentDao.save(comment);
        return false;
    }


    public List<ApplicationComment> getApplicationComment(String applicationId){
        ResultSet execute = session.execute(getApplicationComments.bind());
        PagingIterable<ApplicationComment> convert = applicationCommentDao.convert(execute);
        return convert.all();
    }


}
