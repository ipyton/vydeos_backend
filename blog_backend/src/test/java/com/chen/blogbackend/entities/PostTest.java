package com.chen.blogbackend.entities;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostTest {

    @Test
    void testDefaultConstructor() {
        Post post = new Post();
        
        // Default values
        assertEquals("", post.getAuthorID());
        assertEquals("", post.getAuthorName());
        assertEquals("", post.getContent());
        assertEquals(0, post.getLikes());
        assertNull(post.getPostID());
        assertNull(post.getLastModified());
    }

    @Test
    void testConstructorWithReceiverId() {
        String authorId = "author1";
        String authorName = "John Doe";
        Long postId = 12345L;
        Instant lastModified = Instant.now();
        String content = "Test content";
        int likes = 10;
        List<String> notice = Arrays.asList("user1", "user2");
        List<String> accessRules = Arrays.asList("public");
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
        List<String> voices = Arrays.asList("voice1.mp3");
        List<String> videos = Arrays.asList("video1.mp4");
        List<String> comments = Arrays.asList("comment1", "comment2");
        String receiverId = "receiver1";
        String location = "New York";
        
        Post post = new Post(
                authorId, authorName, postId, lastModified, 
                content, likes, notice, accessRules, 
                images, voices, videos, comments,
                receiverId, location
        );
        
        assertEquals(authorId, post.getAuthorID());
        assertEquals(authorName, post.getAuthorName());
        assertEquals(postId, post.getPostID());
        assertEquals(lastModified, post.getLastModified());
        assertEquals(content, post.getContent());
        assertEquals(likes, post.getLikes());
        assertEquals(notice, post.getNotice());
        assertEquals(accessRules, post.getAccessRules());
        assertEquals(images, post.getImages());
        assertEquals(voices, post.getVoices());
        assertEquals(videos, post.getVideos());
        assertEquals(comments, post.getComments());
        assertEquals(receiverId, post.getReceiverId());
        assertEquals(location, post.getLocation());
    }

    @Test
    void testConstructorWithoutReceiverId() {
        String authorId = "author1";
        String authorName = "John Doe";
        Long postId = 12345L;
        Instant lastModified = Instant.now();
        String content = "Test content";
        int likes = 10;
        List<String> notice = Arrays.asList("user1", "user2");
        List<String> accessRules = Arrays.asList("public");
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
        List<String> voices = Arrays.asList("voice1.mp3");
        List<String> videos = Arrays.asList("video1.mp4");
        List<String> comments = Arrays.asList("comment1", "comment2");
        String location = "New York";
        
        Post post = new Post(
                authorId, authorName, postId, lastModified, 
                content, likes, notice, accessRules, 
                images, voices, videos, comments, location
        );
        
        assertEquals(authorId, post.getAuthorID());
        assertEquals(authorName, post.getAuthorName());
        assertEquals(postId, post.getPostID());
        assertEquals(lastModified, post.getLastModified());
        assertEquals(content, post.getContent());
        assertEquals(likes, post.getLikes());
        assertEquals(notice, post.getNotice());
        assertEquals(accessRules, post.getAccessRules());
        assertEquals(images, post.getImages());
        assertEquals(voices, post.getVoices());
        assertEquals(videos, post.getVideos());
        assertEquals(comments, post.getComments());
        assertEquals(location, post.getLocation());
        assertNull(post.getReceiverId());
    }

    @Test
    void testSettersAndGetters() {
        Post post = new Post();
        
        // Set values
        String authorId = "author2";
        String authorName = "Jane Doe";
        Long postId = 67890L;
        Instant lastModified = Instant.now();
        String content = "Updated content";
        int likes = 20;
        List<String> notice = Arrays.asList("user3", "user4");
        List<String> accessRules = Arrays.asList("private");
        List<String> images = Arrays.asList("image3.jpg");
        List<String> voices = Arrays.asList("voice2.mp3", "voice3.mp3");
        List<String> videos = Arrays.asList("video2.mp4");
        List<String> comments = Arrays.asList("comment3");
        String receiverId = "receiver2";
        String location = "Los Angeles";
        
        post.setAuthorID(authorId);
        post.setAuthorName(authorName);
        post.setPostID(postId);
        post.setLastModified(lastModified);
        post.setContent(content);
        post.setLikes(likes);
        post.setNotice(notice);
        post.setAccessRules(accessRules);
        post.setImages(images);
        post.setVoices(voices);
        post.setVideos(videos);
        post.setComments(comments);
        post.setReceiverId(receiverId);
        post.setLocation(location);
        
        // Verify getters return correct values
        assertEquals(authorId, post.getAuthorID());
        assertEquals(authorName, post.getAuthorName());
        assertEquals(postId, post.getPostID());
        assertEquals(lastModified, post.getLastModified());
        assertEquals(content, post.getContent());
        assertEquals(likes, post.getLikes());
        assertEquals(notice, post.getNotice());
        assertEquals(accessRules, post.getAccessRules());
        assertEquals(images, post.getImages());
        assertEquals(voices, post.getVoices());
        assertEquals(videos, post.getVideos());
        assertEquals(comments, post.getComments());
        assertEquals(receiverId, post.getReceiverId());
        assertEquals(location, post.getLocation());
    }

    @Test
    void testToString() {
        Post post = new Post();
        post.setAuthorID("author1");
        post.setAuthorName("John Doe");
        post.setPostID(12345L);
        post.setContent("Test content");
        
        String toString = post.toString();
        
        // Verify toString contains key information
        assertTrue(toString.contains("author1"));
        assertTrue(toString.contains("John Doe"));
        assertTrue(toString.contains("12345"));
        assertTrue(toString.contains("Test content"));
    }
} 