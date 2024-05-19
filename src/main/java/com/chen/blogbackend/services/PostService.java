package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.ArticleDao;
import com.chen.blogbackend.entities.Post;

import com.chen.blogbackend.entities.Trend;
import com.chen.blogbackend.filters.PostRecognizer;
import com.chen.blogbackend.mappers.TrendsMapper;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.*;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class PostService {
    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    CommentService commentService;

    @Autowired
    FriendsService friendsService;

    @Autowired
    PostRecognizer recognizer;

    @Autowired
    CqlSession session;

    @Autowired
    Jedis jedis;



    PreparedStatement getRangeArticlesByUserId;
    PreparedStatement getIdolsArticles;
    PreparedStatement saveArticle;

    public int pageSize = 10;
    public long timeSlice = 50;
    ArticleDao articleDao;

    @PostConstruct
    public void init() {
//        articleDao = new ArticleMapperBuilder(session).build().getArticleDao();

//        session = CqlSession.builder()
//                .addContactPoint(new InetSocketAddress("192.168.23.129",9042))
//                .withAuthCredentials("cassandra", "cassandra")
//                .withKeyspace(CqlIdentifier.fromCql("post"))
//                .build();
        //saveArticle = session.prepare("insert into ");
        getRangeArticlesByUserId = session.prepare("select * from posts.posts_by_user_id where author_id = ?");
        pageSize = 10;
    }


    public ArrayList<Post> getArticlesByUserID(String userEmail, PagingState state) {
        ResultSet result = session.execute(getRangeArticlesByUserId.bind(":").setPageSize(pageSize).setPagingState(state));
        ArrayList<Post> posts = new ArrayList<>();

        for (Row row : result) {
            posts.add(new Post());

        }
        return posts;
    }

    public List<Post> getPostsByTimestamp(String userId, Instant timestampFrom, Instant timestampTo) {

        return new ArrayList<>();
    }



    public int uploadArticle(String userId, Post post){
        articleDao.save(post);
        return 1;
    }

    public Post getArticleByArticleID(String articleID) {
        return articleDao.findById(articleID);
    }

    private List<Post> getBatchArticles(List<String> targetUsers, String userId) {
        ArrayList<Post> result = new ArrayList<>();
        BatchStatementBuilder batch = BatchStatement.builder(DefaultBatchType.LOGGED);

        for (String id : targetUsers) {
            batch.addStatement(getRangeArticlesByUserId.bind(id));
        }
        BatchStatement build = batch.build();
        ResultSet execute = session.execute(build);
        PagingIterable<Post> articles = articleDao.getArticles(execute);

        for (Post post : articles) {
            Set<String> users = post.getUsers();
            if (post.getAccessType().equals("exclude")) {
                if(!users.contains(userId)){
                    result.add(post);
                }
            }
            else if (null == post.getAccessType() || post.getAccessType().equals("include")) {
                result.add(post);
            }
        }
        return result;
    }

    public List<Trend> getTrends() {
        List<Tuple> trends = jedis.zrangeWithScores("trends", 0, 10);
        return TrendsMapper.parseTrends(trends);

    }



//    public PagingMessage<Article> getArticlesByGroup(String userId, String groupId, Long startIndex) {
//        List<String> friendIdsByGroupId = friendsService.getFriendIdsByGroupId(groupId);
//        ArrayList<String> strings = recognizer.get(friendIdsByGroupId, startIndex, timeSlice);
//        List<Article> res = getBatchArticles(strings, userId);
//        return new PagingMessage<>(res, null, 1, Long.toString(timeSlice));
//    }
//
//    public PagingMessage<Article> getArticlesFollowing(String userId, Long startIndex) {
//        List<Relationship> idolsByUserId = friendsService.getIdolsByUserId(userId);
//        ArrayList<String> strings = recognizer.get(idolsByUserId, startIndex, timeSlice);
//        List<Article> res = getBatchArticles(strings, userId);
//        return new PagingMessage<>(res, null, 1, Long.toString(timeSlice));
//    }

}
