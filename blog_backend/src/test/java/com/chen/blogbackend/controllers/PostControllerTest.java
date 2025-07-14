package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.chen.blogbackend.entities.Post;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.Message;
import com.chen.blogbackend.services.PictureService;
import com.chen.blogbackend.services.PostService;
import com.chen.blogbackend.util.MapboxSearchUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PostControllerTest {

    @Mock
    private PostService postService;
    
    @Mock
    private PictureService pictureService;
    
    @InjectMocks
    private PostController postController;
    
    private MockHttpServletRequest request;
    private Post testPost;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        request = new MockHttpServletRequest();
        request.setAttribute("userEmail", "test@example.com");
        
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
    }

    @Test
    void testDeletePost() {
        // Call the controller method
        LoginMessage result = postController.deletePost(123456789L);
        
        // Verify postService.deletePost was called
        verify(postService, times(1)).deletePost(123456789L);
        
        // Verify response
        assertEquals(-1, result.getCode());
        assertEquals("failed", result.getMessage());
    }

    @Test
    void testUploadPost_Success() {
        // Setup mock
        when(postService.uploadPost(anyString(), any(Post.class))).thenReturn(1);
        
        // Call the controller method
        LoginMessage result = postController.uploadPost(request, testPost);
        
        // Verify postService.uploadPost was called
        verify(postService, times(1)).uploadPost(eq("test@example.com"), any(Post.class));
        
        // Verify the post was updated with the user email from the request
        assertEquals("test@example.com", testPost.getAuthorID());
        
        // Verify response
        assertEquals(1, result.getCode());
        assertEquals("1", result.getMessage());
    }

    @Test
    void testUploadPost_Failure() {
        // Setup mock
        when(postService.uploadPost(anyString(), any(Post.class))).thenReturn(-1);
        
        // Call the controller method
        LoginMessage result = postController.uploadPost(request, testPost);
        
        // Verify response
        assertEquals(-1, result.getCode());
        assertEquals("error", result.getMessage());
    }

    @Test
    void testGetPostByPostId_Success() {
        // Setup mock
        String postId = "123456789";
        when(postService.getPostByPostID(postId)).thenReturn(testPost);
        
        // Call the controller method
        LoginMessage result = postController.getPostByPostId(postId);
        
        // Verify response
        assertEquals(1, result.getCode());
        assertEquals(JSON.toJSONString(testPost), result.getMessage());
    }

    @Test
    void testGetPostByPostId_NotFound() {
        // Setup mock to return null (post not found)
        String postId = "nonexistent";
        when(postService.getPostByPostID(postId)).thenReturn(null);
        
        // Call the controller method
        LoginMessage result = postController.getPostByPostId(postId);
        
        // Verify response
        assertEquals(-1, result.getCode());
        assertEquals("Error", result.getMessage());
    }

    @Test
    void testGetPostsByUserId() {
        // Setup mock
        String userId = "test@example.com";
        String pagingState = null;
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("code", 1);
        
        when(postService.getPostsByUserID(userId, pagingState)).thenReturn(expectedResult);
        
        // Call the controller method
        JSONObject result = postController.getPostsByUserId(userId, pagingState);
        
        // Verify postService.getPostsByUserID was called
        verify(postService, times(1)).getPostsByUserID(userId, pagingState);
        
        // Verify the result is the same as what was returned by the service
        assertEquals(expectedResult, result);
    }

    @Test
    void testGetFriendsPosts() {
        // Setup mock
        String pagingState = null;
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("code", 1);
        
        when(postService.getFriendsPosts("test@example.com", pagingState)).thenReturn(expectedResult);
        
        // Call the controller method
        JSONObject result = postController.getFriendsPosts(request, pagingState);
        
        // Verify postService.getFriendsPosts was called
        verify(postService, times(1)).getFriendsPosts("test@example.com", pagingState);
        
        // Verify the result is the same as what was returned by the service
        assertEquals(expectedResult, result);
    }

    @Test
    void testGetPostsByTimeStamp() {
        // Setup mock
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("userEmail", "test@example.com");
        
        String timestampFrom = "2023-01-01T00:00:00Z";
        String timestampTo = "2023-01-31T23:59:59Z";
        
        ArrayList<Post> expectedPosts = new ArrayList<>();
        expectedPosts.add(testPost);
        
        when(postService.getPostsByTimestamp(
            eq("test@example.com"), 
            eq(Instant.parse(timestampFrom)), 
            eq(Instant.parse(timestampTo))
        )).thenReturn(expectedPosts);
        
        // Call the controller method
        LoginMessage result = postController.getPostsByTimeStamp(request, timestampFrom, timestampTo);
        
        // Verify response
        assertEquals(1, result.getCode());
        assertEquals(JSON.toJSONString(expectedPosts), result.getMessage());
    }

    @Test
    void testSearchLocation() {
        // Setup mock using MockedStatic for the static method
        String keyword = "New York";
        String expectedResponse = "{\"results\": [{\"name\":\"New York City\"}]}";
        
        try (MockedStatic<MapboxSearchUtil> mockedMapbox = mockStatic(MapboxSearchUtil.class)) {
            mockedMapbox.when(() -> MapboxSearchUtil.searchByKeyword(keyword)).thenReturn(expectedResponse);
            
            // Call the controller method
            Message result = postController.searchLocation(keyword);
            
            // Verify response
            assertEquals(0, result.getCode());
            assertEquals(expectedResponse, result.getMessage());
        }
    }

    @Test
    void testFetchPictures() {
        // Setup mock
        String path = "838/8380623876590390108.jpg";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/post/fetch_pictures/" + path);
        
        StreamingResponseBody expectedResponse = outputStream -> outputStream.write("test image data".getBytes());
        when(pictureService.getPostPictures(path)).thenReturn(expectedResponse);
        
        // Call the controller method
        StreamingResponseBody result = postController.fetchPictures(request);
        
        // Verify pictureService.getPostPictures was called with the correct path
        verify(pictureService, times(1)).getPostPictures(path);
        
        // The StreamingResponseBody is a functional interface, so we can't directly compare
        // the expected and actual responses, but we can verify it's not null
        assertNotNull(result);
    }
} 