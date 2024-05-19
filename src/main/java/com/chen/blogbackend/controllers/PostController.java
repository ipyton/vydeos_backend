package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.Post;
import com.chen.blogbackend.responseMessage.LoginMessage;

import com.chen.blogbackend.services.PostService;
import com.chen.blogbackend.services.PictureService;
import com.datastax.oss.driver.api.core.cql.PagingState;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.Instant;
import java.util.ArrayList;
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

    @PostMapping(value = "uploadPostPics")
    public LoginMessage uploadArticlePics(MultipartFile multipartFile, String articleID){
        boolean result = pictureService.uploadAvatarPicture(articleID, multipartFile);
        if(!result) return new LoginMessage(-1, "hello");
        else return new LoginMessage(1, "success");
    }

    @PostMapping("get_pic")
    public ResponseEntity<StreamingResponseBody> getArticlePicture(String articleID, int idx) {
        StreamingResponseBody responseBody = pictureService.getPicture(articleID, idx);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(responseBody);
    }

    @PostMapping("upload_pic")
    public LoginMessage uploadPicture(String articleID, MultipartFile file, int index) {
        if (pictureService.uploadArticlePicture(articleID, file, index)) {
            return new LoginMessage(1, "success");
        }
        return new LoginMessage(-1, "failed");
    }


    @PostMapping("upload")
    public LoginMessage uploadArticle(HttpServletRequest request, Post post) {
        String userEmail = request.getHeader("userEmail");
        int result = postService.uploadArticle(userEmail, post);
        if (-1 != result) {
            return new LoginMessage(1, Integer.toString(result));
        } else {
            return new LoginMessage(-1, "error");
        }
    }

    @PostMapping("get")
    public LoginMessage getArticle(String articleID) {
        Post post = postService.getArticleByArticleID(articleID);
        if(null != postService.getArticleByArticleID(articleID)) {
            return new LoginMessage(1, JSON.toJSONString(post));
        }
        return new LoginMessage(-1, "Error");

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
}
