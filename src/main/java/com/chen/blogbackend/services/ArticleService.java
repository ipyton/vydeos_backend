package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Article;
import com.chen.blogbackend.entities.Friend;

import com.chen.blogbackend.filters.PostRecognizer;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ArticleService {
    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    CommentService commentService;

    @Autowired
    FriendsService friendsService;

    @Autowired
    CqlSession session;

    @Autowired
    PostRecognizer recognizer;

    PreparedStatement getRangeArticles;
    PreparedStatement getArticleById;
    PreparedStatement uploadArticle;
    PreparedStatement getFollowersArticle;
    PreparedStatement getFriendsArticles;

    public int pageSize = 10;
    public Long timeSlice = 5l;

    @PostConstruct
    public void init() {
        getRangeArticles = session.prepare("select * from ");
        getArticleById = session.prepare("");
        uploadArticle = session.prepare("");
        getFollowersArticle = session.prepare("");
        getFriendsArticles = session.prepare("");
        pageSize = 10;
    }


    public ArrayList<Article> getArticlesByUserID(String userEmail, PagingState state) {
        ResultSet result = session.execute(getRangeArticles.bind(":").setPageSize(pageSize).setPagingState(state));
        ArrayList<Article> articles = new ArrayList<>();

        for (Row row : result) {
            articles.add(new Article());

        }
        return articles;
    }

    public int uploadArticle(String userId, Article article){
        session.execute(uploadArticle.bind());
        return 1;
    }

    public Article getArticleByArticleID(String articleID) {
        ResultSet result = session.execute(getArticleById.bind());
        return new Article();
    }

    public ArrayList<Article> getArticlesByFollowingAndFriends(String userEmail,int from, int to){
        ArrayList<Article> result = new ArrayList<>();
        ArrayList<Article> articlesFollowing = getArticlesFollowing(userEmail, from, to);
        ArrayList<Article> articlesFriends = getArticlesOfFriends(userEmail, from , to);
        return new ArrayList<>();
    }

    public ArrayList<Article> getArticlesByGroup(String userId,String groupId, Long startIndex) {
        ArrayList<Article> result = new ArrayList<>();
        ArrayList<String> friendIdsByGroupId = friendsService.getFriendIdsByGroupId(userId, groupId);
        ArrayList<String> strings = recognizer.get(friendIdsByGroupId, startIndex, timeSlice);


        return result;
    }

    public ArrayList<Article> getArticlesFollowing(String userEmail, int from, int to) {

        return new ArrayList<>();
    }

    public ArrayList<Article> getArticlesOfFriends(String userEmail, int from, int to) {
        ArrayList<Friend> friendByUserId = friendsService.getFriendsByUserId(userEmail);
        ArrayList<Article> result = new ArrayList<>();
        return result;
    }

}
