package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Post;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostParserTest {

    @Mock
    private ResultSet resultSet;
    
    @Mock
    private Row rowWithReceiverId;
    
    @Mock
    private Row rowWithoutReceiverId;
    
    @Mock
    private ColumnDefinitions columnDefsWithReceiverId;
    
    @Mock
    private ColumnDefinitions columnDefsWithoutReceiverId;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup for row with receiver_id
        when(rowWithReceiverId.getColumnDefinitions()).thenReturn(columnDefsWithReceiverId);
        when(columnDefsWithReceiverId.contains("receiver_id")).thenReturn(true);
        
        // Post data with receiver_id
        when(rowWithReceiverId.getString("author_id")).thenReturn("author1");
        when(rowWithReceiverId.getString("author_name")).thenReturn("John Doe");
        when(rowWithReceiverId.getLong("post_id")).thenReturn(12345L);
        when(rowWithReceiverId.getInstant("last_modified")).thenReturn(Instant.now());
        when(rowWithReceiverId.getString("content")).thenReturn("Test content");
        when(rowWithReceiverId.getInt("likes")).thenReturn(10);
        when(rowWithReceiverId.getList("notice", String.class)).thenReturn(Arrays.asList("user1", "user2"));
        when(rowWithReceiverId.getList("accessRules", String.class)).thenReturn(Arrays.asList("public"));
        when(rowWithReceiverId.getList("images", String.class)).thenReturn(Arrays.asList("image1.jpg"));
        when(rowWithReceiverId.getList("voices", String.class)).thenReturn(Arrays.asList("voice1.mp3"));
        when(rowWithReceiverId.getList("videos", String.class)).thenReturn(Arrays.asList("video1.mp4"));
        when(rowWithReceiverId.getList("comments", String.class)).thenReturn(Arrays.asList("comment1"));
        when(rowWithReceiverId.getString("receiver_id")).thenReturn("receiver1");
        when(rowWithReceiverId.getString("location")).thenReturn("New York");
        
        // Setup column presence
        when(columnDefsWithReceiverId.contains("author_id")).thenReturn(true);
        when(columnDefsWithReceiverId.contains("author_name")).thenReturn(true);
        when(columnDefsWithReceiverId.contains("post_id")).thenReturn(true);
        when(columnDefsWithReceiverId.contains("last_modified")).thenReturn(true);
        when(columnDefsWithReceiverId.contains("content")).thenReturn(true);
        when(columnDefsWithReceiverId.contains("likes")).thenReturn(true);
        when(columnDefsWithReceiverId.contains("notice")).thenReturn(true);
        when(columnDefsWithReceiverId.contains("accessRules")).thenReturn(true);
        when(columnDefsWithReceiverId.contains("images")).thenReturn(true);
        when(columnDefsWithReceiverId.contains("voices")).thenReturn(true);
        when(columnDefsWithReceiverId.contains("videos")).thenReturn(true);
        when(columnDefsWithReceiverId.contains("comments")).thenReturn(true);
        when(columnDefsWithReceiverId.contains("location")).thenReturn(true);
        
        // Setup for row without receiver_id
        when(rowWithoutReceiverId.getColumnDefinitions()).thenReturn(columnDefsWithoutReceiverId);
        when(columnDefsWithoutReceiverId.contains("receiver_id")).thenReturn(false);
        
        // Post data without receiver_id
        when(rowWithoutReceiverId.getString("author_id")).thenReturn("author2");
        when(rowWithoutReceiverId.getString("author_name")).thenReturn("Jane Doe");
        when(rowWithoutReceiverId.getLong("post_id")).thenReturn(67890L);
        when(rowWithoutReceiverId.getInstant("last_modified")).thenReturn(Instant.now());
        when(rowWithoutReceiverId.getString("content")).thenReturn("Another content");
        when(rowWithoutReceiverId.getInt("likes")).thenReturn(20);
        when(rowWithoutReceiverId.getList("notice", String.class)).thenReturn(Arrays.asList("user3"));
        when(rowWithoutReceiverId.getList("accessRules", String.class)).thenReturn(Arrays.asList("private"));
        when(rowWithoutReceiverId.getList("images", String.class)).thenReturn(Arrays.asList("image2.jpg"));
        when(rowWithoutReceiverId.getList("voices", String.class)).thenReturn(Arrays.asList("voice2.mp3"));
        when(rowWithoutReceiverId.getList("videos", String.class)).thenReturn(Arrays.asList("video2.mp4"));
        when(rowWithoutReceiverId.getList("comments", String.class)).thenReturn(Arrays.asList("comment2"));
        when(rowWithoutReceiverId.getString("location")).thenReturn("Los Angeles");
        
        // Setup column presence
        when(columnDefsWithoutReceiverId.contains("author_id")).thenReturn(true);
        when(columnDefsWithoutReceiverId.contains("author_name")).thenReturn(true);
        when(columnDefsWithoutReceiverId.contains("post_id")).thenReturn(true);
        when(columnDefsWithoutReceiverId.contains("last_modified")).thenReturn(true);
        when(columnDefsWithoutReceiverId.contains("content")).thenReturn(true);
        when(columnDefsWithoutReceiverId.contains("likes")).thenReturn(true);
        when(columnDefsWithoutReceiverId.contains("notice")).thenReturn(true);
        when(columnDefsWithoutReceiverId.contains("accessRules")).thenReturn(true);
        when(columnDefsWithoutReceiverId.contains("images")).thenReturn(true);
        when(columnDefsWithoutReceiverId.contains("voices")).thenReturn(true);
        when(columnDefsWithoutReceiverId.contains("videos")).thenReturn(true);
        when(columnDefsWithoutReceiverId.contains("comments")).thenReturn(true);
        when(columnDefsWithoutReceiverId.contains("location")).thenReturn(true);
    }

    @Test
    void testUserDetailParser_WithReceiverIdRow() {
        // Setup result set with one row that has receiver_id
        when(resultSet.all()).thenReturn(Arrays.asList(rowWithReceiverId));
        
        // Execute the parser
        List<Post> posts = PostParser.userDetailParser(resultSet);
        
        // Verify
        assertNotNull(posts);
        assertEquals(1, posts.size());
        
        Post post = posts.get(0);
        assertEquals("author1", post.getAuthorID());
        assertEquals("John Doe", post.getAuthorName());
        assertEquals(12345L, post.getPostID());
        assertNotNull(post.getLastModified());
        assertEquals("Test content", post.getContent());
        assertEquals(10, post.getLikes());
        assertEquals(Arrays.asList("user1", "user2"), post.getNotice());
        assertEquals(Arrays.asList("public"), post.getAccessRules());
        assertEquals(Arrays.asList("image1.jpg"), post.getImages());
        assertEquals(Arrays.asList("voice1.mp3"), post.getVoices());
        assertEquals(Arrays.asList("video1.mp4"), post.getVideos());
        assertEquals(Arrays.asList("comment1"), post.getComments());
        assertEquals("receiver1", post.getReceiverId());
        assertEquals("New York", post.getLocation());
    }

    @Test
    void testUserDetailParser_WithoutReceiverIdRow() {
        // Setup result set with one row that doesn't have receiver_id
        when(resultSet.all()).thenReturn(Arrays.asList(rowWithoutReceiverId));
        
        // Execute the parser
        List<Post> posts = PostParser.userDetailParser(resultSet);
        
        // Verify
        assertNotNull(posts);
        assertEquals(1, posts.size());
        
        Post post = posts.get(0);
        assertEquals("author2", post.getAuthorID());
        assertEquals("Jane Doe", post.getAuthorName());
        assertEquals(67890L, post.getPostID());
        assertNotNull(post.getLastModified());
        assertEquals("Another content", post.getContent());
        assertEquals(20, post.getLikes());
        assertEquals(Arrays.asList("user3"), post.getNotice());
        assertEquals(Arrays.asList("private"), post.getAccessRules());
        assertEquals(Arrays.asList("image2.jpg"), post.getImages());
        assertEquals(Arrays.asList("voice2.mp3"), post.getVoices());
        assertEquals(Arrays.asList("video2.mp4"), post.getVideos());
        assertEquals(Arrays.asList("comment2"), post.getComments());
        assertEquals("Los Angeles", post.getLocation());
        assertNull(post.getReceiverId());
    }

    @Test
    void testUserDetailParser_MultipleRows() {
        // Setup result set with both types of rows
        when(resultSet.all()).thenReturn(Arrays.asList(rowWithReceiverId, rowWithoutReceiverId));
        
        // Execute the parser
        List<Post> posts = PostParser.userDetailParser(resultSet);
        
        // Verify
        assertNotNull(posts);
        assertEquals(2, posts.size());
        
        // First post (with receiver_id)
        Post post1 = posts.get(0);
        assertEquals("author1", post1.getAuthorID());
        assertEquals("receiver1", post1.getReceiverId());
        
        // Second post (without receiver_id)
        Post post2 = posts.get(1);
        assertEquals("author2", post2.getAuthorID());
        assertNull(post2.getReceiverId());
    }

    @Test
    void testUserDetailParser_EmptyResultSet() {
        // Setup empty result set
        when(resultSet.all()).thenReturn(Arrays.asList());
        
        // Execute the parser
        List<Post> posts = PostParser.userDetailParser(resultSet);
        
        // Verify
        assertNotNull(posts);
        assertTrue(posts.isEmpty());
    }

    @Test
    void testUserDetailParser_MissingColumns() {
        // Setup columns missing in the row
        when(columnDefsWithoutReceiverId.contains("author_name")).thenReturn(false);
        when(columnDefsWithoutReceiverId.contains("content")).thenReturn(false);
        
        when(resultSet.all()).thenReturn(Arrays.asList(rowWithoutReceiverId));
        
        // Execute the parser
        List<Post> posts = PostParser.userDetailParser(resultSet);
        
        // Verify
        assertNotNull(posts);
        assertEquals(1, posts.size());
        
        Post post = posts.get(0);
        assertNull(post.getAuthorName()); // Missing column should result in null
        assertNull(post.getContent()); // Missing column should result in null
        assertEquals("author2", post.getAuthorID()); // This column exists
    }
} 