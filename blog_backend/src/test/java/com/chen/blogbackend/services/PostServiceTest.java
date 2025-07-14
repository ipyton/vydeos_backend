package com.chen.blogbackend.services;

import com.alibaba.fastjson2.JSONObject;
import com.chen.blogbackend.entities.Post;
import com.chen.blogbackend.entities.Relationship;
import com.chen.blogbackend.entities.Trend;
import com.chen.blogbackend.filters.PostRecognizer;
import com.chen.blogbackend.mappers.TrendsMapper;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Mock
    private CqlSession session;
    
    @Mock
    private Jedis jedis;
    
    @Mock
    private CommentService commentService;
    
    @Mock
    private FriendsService friendsService;
    
    @Mock
    private PostRecognizer recognizer;
    
    @Mock
    private PreparedStatement getPostByIdMock;
    
    @Mock
    private PreparedStatement getPostByUserIdMock;
    
    @Mock
    private PreparedStatement savePostByIdMock;
    
    @Mock
    private PreparedStatement savePostByUserIdMock;
    
    @Mock
    private PreparedStatement sendToMailboxMock;
    
    @Mock
    private PreparedStatement getMailBoxMock;
    
    @Mock
    private PreparedStatement deleteTempPicsMock;
    
    @Mock
    private PreparedStatement getRangeArticlesByUserIdMock;
    
    @Mock
    private BoundStatement boundStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private ExecutionInfo executionInfo;
    
    @InjectMocks
    private PostService postService;
    
    private Post testPost;
    private List<Relationship> friendsList;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock session.prepare() calls
        when(session.prepare("select * from posts.posts_by_user_id where author_id = ?")).thenReturn(getRangeArticlesByUserIdMock);
        when(session.prepare("insert into posts.posts_by_post_id (post_id, likes, author_id,  author_name,  comments, last_modified, images , videos , voices  , content , access_rules , notice, location ) values(?,?,?,?,?,?,?,?,?,?,?,?,?)")).thenReturn(savePostByIdMock);
        when(session.prepare("insert into posts.posts_by_user_id (post_id, likes, author_id, author_name, comments, last_modified, images, videos, voices, content, access_rules, notice, location) values(?,?,?,?,?,?,?,?,?,?,?,?,?)")).thenReturn(savePostByUserIdMock);
        when(session.prepare("insert into posts.mail_box (receiver_id, last_modified, likes, comments,content, author_id,author_name,  images , videos , voices  , post_id , notice , access_rules , location) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)")).thenReturn(sendToMailboxMock);
        when(session.prepare("select * from posts.posts_by_post_id where post_id = ? ")).thenReturn(getPostByIdMock);
        when(session.prepare("select * from posts.posts_by_user_id where author_id = ? order by last_modified  desc")).thenReturn(getPostByUserIdMock);
        when(session.prepare("select * from posts.mail_box where receiver_id = ? order by last_modified desc;")).thenReturn(getMailBoxMock);
        when(session.prepare("delete from posts.temporary_post_pics where author_id = ?")).thenReturn(deleteTempPicsMock);
        
        // Setup test post
        testPost = new Post();
        testPost.setAuthorID("test@example.com");
        testPost.setAuthorName("Test User");
        testPost.setPostID(123456789L);
        testPost.setContent("Test content");
        testPost.setLastModified(Instant.now());
        testPost.setLikes(10);
        testPost.setNotice(Arrays.asList("user1"));
        testPost.setAccessRules(Arrays.asList("public"));
        testPost.setImages(Arrays.asList("image1.jpg"));
        testPost.setVideos(new ArrayList<>());
        testPost.setVoices(new ArrayList<>());
        testPost.setComments(Arrays.asList("comment1"));
        testPost.setLocation("Test Location");
        
        // Setup friends list
        Relationship friend1 = new Relationship();
        friend1.setFriendId("friend1@example.com");
        friend1.setUserId("test@example.com");
        
        Relationship friend2 = new Relationship();
        friend2.setFriendId("friend2@example.com");
        friend2.setUserId("test@example.com");
        
        friendsList = Arrays.asList(friend1, friend2);
        
        // Initialize service manually (since @PostConstruct won't be called in tests)
        postService.init();
    }

    @Test
    void testUploadPost() {
        // Setup mocks
        when(friendsService.getFriendsByUserId("test@example.com")).thenReturn(friendsList);
        
        // Mock bound statements
        when(savePostByIdMock.bind(any(), anyInt(), anyString(), anyString(), anyList(), any(Instant.class), anyList(), anyList(), anyList(), anyString(), anyList(), anyList(), anyString())).thenReturn(boundStatement);
        when(savePostByUserIdMock.bind(any(), anyInt(), anyString(), anyString(), anyList(), any(Instant.class), anyList(), anyList(), anyList(), anyString(), anyList(), anyList(), anyString())).thenReturn(boundStatement);
        when(deleteTempPicsMock.bind(anyString())).thenReturn(boundStatement);
        when(sendToMailboxMock.bind(anyString(), any(Instant.class), anyInt(), anyList(), anyString(), anyString(), anyString(), anyList(), anyList(), anyList(), any(), anyList(), anyList(), anyString())).thenReturn(boundStatement);
        
        // Mock batch statement building
        when(session.execute(any(BatchStatement.class))).thenReturn(resultSet);
        
        // Call the method
        int result = postService.uploadPost("test@example.com", testPost);
        
        // Verify
        assertEquals(1, result);
        verify(session, times(1)).execute(any(BatchStatement.class));
        // Verify that statements were bound with the correct parameters
        verify(savePostByIdMock, times(1)).bind(
            eq(testPost.getPostID()), eq(testPost.getLikes()), eq(testPost.getAuthorID()),
            eq(testPost.getAuthorName()), eq(testPost.getComments()), eq(testPost.getLastModified()),
            eq(testPost.getImages()), eq(testPost.getVideos()), eq(testPost.getVoices()),
            eq(testPost.getContent()), eq(testPost.getAccessRules()), eq(testPost.getNotice()),
            eq(testPost.getLocation())
        );
    }

    @Test
    void testGetPostByPostID() {
        // Setup mock
        String postId = "123456789";
        when(getPostByIdMock.bind(postId)).thenReturn(boundStatement);
        when(session.execute(boundStatement)).thenReturn(resultSet);
        
        // Mock the row data for PostParser to use
        // This is simplified; in a real test you'd need to mock the complete row structure
        // or use a mocked static PostParser
        List<Post> mockPosts = Arrays.asList(testPost);
        
        try (MockedStatic<com.chen.blogbackend.mappers.PostParser> mockedParser = mockStatic(com.chen.blogbackend.mappers.PostParser.class)) {
            mockedParser.when(() -> com.chen.blogbackend.mappers.PostParser.userDetailParser(any(ResultSet.class))).thenReturn(mockPosts);
            
            // Call method
            Post result = postService.getPostByPostID(postId);
            
            // Verify
            assertNotNull(result);
            assertEquals(testPost.getAuthorID(), result.getAuthorID());
            assertEquals(testPost.getPostID(), result.getPostID());
        }
    }

    @Test
    void testGetPostsByUserID() {
        // Setup mocks
        String userEmail = "test@example.com";
        String pagingState = null;
        
        when(getRangeArticlesByUserIdMock.bind(userEmail)).thenReturn(boundStatement);
        when(session.execute(boundStatement)).thenReturn(resultSet);
        
        // Mock execution info for paging
        ByteBuffer mockPagingState = ByteBuffer.wrap("next_page".getBytes());
        when(resultSet.getExecutionInfo()).thenReturn(executionInfo);
        when(executionInfo.getPagingState()).thenReturn(mockPagingState);
        
        // Mock the post parser results
        List<Post> postList = Arrays.asList(testPost);
        try (MockedStatic<com.chen.blogbackend.mappers.PostParser> mockedParser = mockStatic(com.chen.blogbackend.mappers.PostParser.class)) {
            mockedParser.when(() -> com.chen.blogbackend.mappers.PostParser.userDetailParser(resultSet)).thenReturn(postList);
            
            // Call method
            JSONObject result = postService.getPostsByUserID(userEmail, pagingState);
            
            // Verify
            assertNotNull(result);
            assertEquals(1, result.getIntValue("code"));
            assertNotNull(result.getString("pagingState"));
        }
    }

    @Test
    void testGetFriendsPosts() {
        // Setup mocks
        String userEmail = "test@example.com";
        String pagingState = null;
        
        when(getMailBoxMock.bind(userEmail)).thenReturn(boundStatement);
        when(boundStatement.setPageSize(anyInt())).thenReturn(boundStatement);
        when(session.execute(boundStatement)).thenReturn(resultSet);
        
        // Mock execution info for paging
        ByteBuffer mockPagingState = ByteBuffer.wrap("next_page".getBytes());
        when(resultSet.getExecutionInfo()).thenReturn(executionInfo);
        when(executionInfo.getPagingState()).thenReturn(mockPagingState);
        
        // Mock the post parser results
        List<Post> postList = Arrays.asList(testPost);
        try (MockedStatic<com.chen.blogbackend.mappers.PostParser> mockedParser = mockStatic(com.chen.blogbackend.mappers.PostParser.class)) {
            mockedParser.when(() -> com.chen.blogbackend.mappers.PostParser.userDetailParser(resultSet)).thenReturn(postList);
            
            // Call method
            JSONObject result = postService.getFriendsPosts(userEmail, pagingState);
            
            // Verify
            assertNotNull(result);
            assertEquals(1, result.getIntValue("code"));
            assertNotNull(result.getString("pagingState"));
        }
    }

    @Test
    void testGetTrends() {
        // Setup mock trends from Redis
        List<Tuple> redisTrends = new ArrayList<>();
        // Mock Redis response
        when(jedis.zrangeWithScores("trends", 0, 10)).thenReturn(redisTrends);
        
        List<Trend> expectedTrends = new ArrayList<>();
        // Mock the TrendsMapper static method
        try (MockedStatic<TrendsMapper> mockedMapper = mockStatic(TrendsMapper.class)) {
            mockedMapper.when(() -> TrendsMapper.parseTrends(redisTrends)).thenReturn(expectedTrends);
            
            // Call method
            List<Trend> result = postService.getTrends();
            
            // Verify
            assertNotNull(result);
            assertEquals(expectedTrends, result);
        }
    }

    @Test
    void testDeletePost() {
        // Simply verify that the method is called
        postService.deletePost(123456789L);
        // This is just a placeholder test as the actual method is empty
    }

    @Test
    void testGetPostsByTimestamp() {
        String userId = "test@example.com";
        Instant timeFrom = Instant.parse("2023-01-01T00:00:00Z");
        Instant timeTo = Instant.parse("2023-01-31T23:59:59Z");
        
        // Call method - returns empty list by default
        List<Post> result = postService.getPostsByTimestamp(userId, timeFrom, timeTo);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
} 