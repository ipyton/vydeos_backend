package com.chen.blogbackend.services;

import com.alibaba.fastjson2.JSONObject;
import com.chen.blogbackend.DAO.ArticleDao;
import com.chen.blogbackend.entities.Post;

import com.chen.blogbackend.entities.Relationship;
import com.chen.blogbackend.entities.Trend;
import com.chen.blogbackend.filters.PostRecognizer;
import com.chen.blogbackend.mappers.PostParser;
import com.chen.blogbackend.mappers.TrendsMapper;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class PostService {

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


    private PreparedStatement getRangeArticlesByUserId;
    private PreparedStatement getIdolsArticles;
    private PreparedStatement savePostById;
    private PreparedStatement getPostById;
    private PreparedStatement getPostByUserId;
    private PreparedStatement savePostByUserId;
    private PreparedStatement sendToMailbox;
    private PreparedStatement getMailBox;


    private PreparedStatement deleteTempPics;
    private int pageSize = 10;
    private long timeSlice = 50;


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
        savePostById= session.prepare("insert into posts.posts_by_post_id (post_id, likes, author_id,  author_name,  comments, last_modified, images , videos , voices  , content , access_rules , notice, location ) values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
        savePostByUserId = session.prepare("insert into posts.posts_by_user_id (post_id, likes, author_id, author_name, comments, last_modified, images, videos, voices, content, access_rules, notice, location) values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
        sendToMailbox =session.prepare("insert into posts.mail_box (receiver_id, last_modified, likes, comments,content, author_id,author_name,  images , videos , voices  , post_id , notice , access_rules , location) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        getPostById = session.prepare("select * from posts.posts_by_post_id where post_id = ? ");
        getPostByUserId = session.prepare("select * from posts.posts_by_user_id where author_id = ? order by last_modified  desc");
        getMailBox = session.prepare("select * from posts.mail_box where receiver_id = ? order by last_modified desc;");
        deleteTempPics = session.prepare("delete from posts.temporary_post_pics where author_id = ?");
        pageSize = 10;
    }


    public JSONObject getPostsByUserID(String userEmail, String state) {
        ResultSet result;
        if (state == null) {
            result = session.execute(getRangeArticlesByUserId.bind(userEmail));
        }
        else {
            PagingState pagingState = PagingState.fromString(state);
            result = session.execute(getRangeArticlesByUserId.bind(userEmail).setPageSize(pageSize).setPagingState(pagingState));
        }

        JSONObject jsonObject  = processResults(result);
        return jsonObject;
    }

    private JSONObject processResults(ResultSet result) {
        ByteBuffer pagingState = result.getExecutionInfo().getPagingState();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("posts", PostParser.userDetailParser(result));
        if (pagingState!= null) {
            jsonObject.put("pagingState", pagingState.toString());
        }

        jsonObject.put("code", 1);
        System.out.println(jsonObject.toJSONString());
        return jsonObject;
    }

    public List<Post> getPostsByTimestamp(String userId, Instant timestampFrom, Instant timestampTo) {

        return new ArrayList<>();
    }



    public int uploadPost(String userId, Post post){
        System.out.println(post);
        BatchStatementBuilder builder = BatchStatement.builder(DefaultBatchType.LOGGED);
        builder.addStatement(savePostById.bind(post.getPostID(), post.getLikes(),post.getAuthorID(), post.getAuthorName(),
                post.getComments(), post.getLastModified(), post.getImages(), post.getVideos(), post.getVoices(),
                post.getContent(), post.getAccessRules(),post.getNotice(),post.getLocation()));
        builder.addStatement(savePostByUserId.bind(post.getPostID(), post.getLikes(),post.getAuthorID(), post.getAuthorName(),
                post.getComments(), post.getLastModified(), post.getImages(), post.getVideos(), post.getVoices(),
                post.getContent(), post.getAccessRules(),post.getNotice(),post.getLocation()));
        builder.addStatement(deleteTempPics.bind(userId));
        List<Relationship> friends = friendsService.getFriendsByUserId(userId);
        for (Relationship friend : friends) {
            String friendId = friend.getFriendId();
            builder.addStatement(sendToMailbox.bind(friendId, post.getLastModified(), post.getLikes(), post.getComments(), post.getContent(),
                    post.getAuthorID(), post.getAuthorName(), post.getImages(), post.getVideos(), post.getVoices(), post.getPostID(),
                    post.getNotice(), post.getAccessRules(), post.getLocation()));
        }
        session.execute(builder.build());
        return 1;
    }

    public JSONObject getFriendsPosts(String userEmail, String pagingState) {
        //        List<Relationship> friends = friendsService.getFriends(userEmail);
//        BatchStatementBuilder builder = BatchStatement.builder(DefaultBatchType.LOGGED);
//        for (Relationship friend : friends) {
//            builder.addStatement(getPostByUserId.bind(friend.getFriendId()));
//        }
//        ResultSet execute = session.execute(builder.build());
        ResultSet result;

        if (pagingState == null) {
             result = session.execute(getMailBox.bind(userEmail).setPageSize(pageSize));
        }
        else {
            PagingState pagingState1 = PagingState.fromString(pagingState);
             result = session.execute(getMailBox.bind(userEmail).setPageSize(pageSize).setPagingState(pagingState1));
        }
        JSONObject jsonObject = processResults(result);
        return jsonObject;

    }

    public Post getPostByPostID(String postId) {
        ResultSet execute = session.execute(getPostById.bind(postId));
        return PostParser.userDetailParser(execute).get(0);
    }


    //    get articles from target users.
//    private List<Post> getBatchArticles(List<String> targetUsers, String userId) {
//        ArrayList<Post> result = new ArrayList<>();
//        BatchStatementBuilder batch = BatchStatement.builder(DefaultBatchType.LOGGED);
//
//        for (String id : targetUsers) {
//            batch.addStatement(getRangeArticlesByUserId.bind(id));
//        }
//        BatchStatement build = batch.build();
//        ResultSet execute = session.execute(build);
//        PagingIterable<Post> articles = articleDao.getArticles(execute);
//
//        for (Post post : articles) {
//            List<String> users = post.getUsers();
//            if (post.getAccessType().equals("exclude")) {
//                if(!users.contains(userId)){
//                    result.add(post);
//                }
//            }
//            else if (null == post.getAccessType() || post.getAccessType().equals("include")) {
//                result.add(post);
//            }
//        }
//        return result;
//    }

    public List<Trend> getTrends() {
        List<Tuple> trends = jedis.zrangeWithScores("trends", 0, 10);
        return TrendsMapper.parseTrends(trends);

    }

    public String preparePostId(){
        return "";
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
