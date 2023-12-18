package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.ArticleDao;
import com.chen.blogbackend.entities.Article;
import com.chen.blogbackend.entities.Friend;

import com.chen.blogbackend.filters.PostRecognizer;
import com.chen.blogbackend.mappers.ArticleMapperBuilder;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.*;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    PreparedStatement getRangeArticlesByUserId;
    PreparedStatement getIdolsArticles;

    public int pageSize = 10;
    public long timeSlice = 50;
    ArticleDao articleDao;

    @PostConstruct
    public void init() {
        articleDao = new ArticleMapperBuilder(session).build().getArticleDao();
        getRangeArticlesByUserId = session.prepare("select * from articles_by_user_id where article_id = ?");
        pageSize = 10;
    }


    public ArrayList<Article> getArticlesByUserID(String userEmail, PagingState state) {
        ResultSet result = session.execute(getRangeArticlesByUserId.bind(":").setPageSize(pageSize).setPagingState(state));
        ArrayList<Article> articles = new ArrayList<>();

        for (Row row : result) {
            articles.add(new Article());

        }
        return articles;
    }

    public int uploadArticle(String userId, Article article){
        articleDao.save(article);
        return 1;
    }

    public Article getArticleByArticleID(String articleID) {
        return articleDao.findById(articleID);
    }

    private List<Article> getBatchArticles(List<String> targetUsers, String userId) {
        ArrayList<Article> result = new ArrayList<>();
        BatchStatementBuilder batch = BatchStatement.builder(DefaultBatchType.LOGGED);

        for (String id : targetUsers) {
            batch.addStatement(getRangeArticlesByUserId.bind(id));
        }
        BatchStatement build = batch.build();
        ResultSet execute = session.execute(build);
        PagingIterable<Article> articles = articleDao.getArticles(execute);

        for (Article article : articles) {
            Set<String> users = article.getUsers();
            if (article.getAccessType().equals("exclude")) {
                if(!users.contains(userId)){
                    result.add(article);
                }
            }
            else if (null == article.getAccessType() || article.getAccessType().equals("include")) {
                result.add(article);
            }
        }
        return result;
    }

    public PagingMessage<Article> getArticlesByGroup(String userId, String groupId, Long startIndex) {
        List<String> friendIdsByGroupId = friendsService.getFriendIdsByGroupId(groupId);
        ArrayList<String> strings = recognizer.get(friendIdsByGroupId, startIndex, timeSlice);
        List<Article> res = getBatchArticles(strings, userId);
        return new PagingMessage<>(res, null, 1, Long.toString(timeSlice));
    }

    public PagingMessage<Article> getArticlesFollowing(String userId, Long startIndex) {
        List<String> idolsByUserId = friendsService.getIdolIdsByUserId(userId);
        ArrayList<String> strings = recognizer.get(idolsByUserId, startIndex, timeSlice);
        List<Article> res = getBatchArticles(strings, userId);
        return new PagingMessage<>(res, null, 1, Long.toString(timeSlice));
    }

}
