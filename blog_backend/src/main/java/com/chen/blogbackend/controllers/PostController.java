package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.chen.blogbackend.entities.Post;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.Message;
import com.chen.blogbackend.services.PostService;
import com.chen.blogbackend.services.PictureService;
import com.chen.blogbackend.util.MapboxSearchUtil;
import com.chen.blogbackend.util.RandomUtil;
import com.datastax.oss.driver.api.core.cql.PagingState;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.time.Instant;
import java.util.List;

@RequestMapping("post")
@Controller()
@ResponseBody
public class PostController {

    @Autowired
    PictureService pictureService;


    @Autowired
    PostService postService;

    private final static int page_size = 10;

    TimeBasedGenerator timeBasedGenerator = Generators.timeBasedGenerator();

    @PostMapping("delete")
    public LoginMessage deletePost(Long postId) {
        postService.deletePost(postId);
        return new LoginMessage(-1, "failed");
    }

    @PostMapping("upload")
    public LoginMessage uploadPost(HttpServletRequest request, @RequestBody Post post) {
        String userEmail = (String) request.getAttribute("userEmail");
        post.setAuthorID(userEmail);
        Long uuid = RandomUtil.generateTimeBasedRandomLong(userEmail);
        post.setPostID(uuid);
        post.setLastModified(Instant.now());
        int result = postService.uploadPost(userEmail, post);
        if (-1 != result) {
            return new LoginMessage(1, Integer.toString(result));
        } else {
            return new LoginMessage(-1, "error");
        }
    }

    @PostMapping("get_by_post_id")
    public LoginMessage getPostByPostId(String postID) {
        Post post = postService.getPostByPostID(postID);
        if(null != postService.getPostByPostID(postID)) {
            return new LoginMessage(1, JSON.toJSONString(post));
        }
        return new LoginMessage(-1, "Error");

    }

    @PostMapping("get_by_user_id")
    public JSONObject getPostsByUserId(String userID, String pagingState) {
        return postService.getPostsByUserID(userID,pagingState);
    }

    @PostMapping("get_friends_posts")
    public JSONObject getFriendsPosts(HttpServletRequest request, String pagingState) {
        String email = (String) request.getAttribute("userEmail");
        return postService.getFriendsPosts(email, pagingState);
    }

    @PostMapping("get_posts_by_timestamp")
    public LoginMessage getPostsByTimeStamp(HttpServletRequest request, String timestampFrom, String timestampTo) {
        String userEmail = request.getHeader("userEmail");
        CharSequence sequenceFrom = timestampFrom;
        Instant timeFrom = Instant.parse(sequenceFrom);
        Instant timeTo = Instant.parse((CharSequence) timestampTo);
        List<Post> postsByTimestamp = postService.getPostsByTimestamp(userEmail, timeFrom, timeTo);
        return new LoginMessage(1, JSON.toJSONString(postsByTimestamp));
    }

    @PostMapping("get_posts_range")
    public LoginMessage getPagingArticles(String userID, PagingState state) {
        return new LoginMessage(-1, "Error");
    }

    @PostMapping("get_recommend_posts")
    public LoginMessage getPagingRecommendArticles(String userID, int from, int to) {
        return new LoginMessage(-1, "error");
    }

    @PostMapping("get_trends")
    public LoginMessage getTrends(){
        return new LoginMessage(1, JSON.toJSONString(postService.getTrends()));
    }

    @PostMapping("add_post")
    public LoginMessage addPost(String introduction) {
        return new LoginMessage(-1,introduction);
    }

    @PostMapping("searchLocation")
    public Message searchLocation(String keyword) {
        String s = MapboxSearchUtil.searchByKeyword(keyword);
        System.out.println(s);
        return new Message(0,s);
    }


    @GetMapping("/fetch_pictures/**")
    public StreamingResponseBody fetchPictures(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        // Since nginx strips /java, the URI will be /post/fetch_pictures/838/8380623876590390108.jpg
        // We need to extract everything after /fetch_pictures/
        int startIndex = requestURI.indexOf("/fetch_pictures/") + "/fetch_pictures/".length();
        String path = requestURI.substring(startIndex);

        // path will now be: "838/8380623876590390108.jpg"
        StreamingResponseBody postPictures = pictureService.getPostPictures(path);
        return postPictures;
    }

//    @PostMapping("uploadPicture")
//    public Message uploadPicture(HttpServletRequest request, MultipartFile file) {
//        return new Message();
//
//    }

}
