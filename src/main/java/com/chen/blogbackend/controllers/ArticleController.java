package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.ResponseMessage.Message;
import com.chen.blogbackend.entities.Article;

import com.chen.blogbackend.services.ArticleService;
import com.chen.blogbackend.services.PictureService;
import jakarta.servlet.http.HttpServletRequest;
import okhttp3.Response;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.ArrayList;

@Controller("article")
public class ArticleController {

    @Autowired
    PictureService pictureService;

    @Autowired
    ArticleService articleService;
    const int page_size = 10;

    @PostMapping(value = "/uploadArticlePics")
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


    @PostMapping("upload_article")
    public LoginMessage uploadArticle(HttpServletRequest request, Article article) {
        String userEmail = request.getHeader("userEmail");
        int result = articleService.uploadArticle(userEmail, article);
        if (-1 != result) {
            return new LoginMessage(1, Integer.toString(result));
        } else {
            return new LoginMessage(-1, "error");
        }
    }

    @PostMapping("get_article")
    public LoginMessage getArticle(String articleID) {
        Article article = articleService.getArticleByArticleID(articleID);
        if(null != articleService.getArticleByArticleID(articleID)) {
            return new LoginMessage(1, JSON.toJSONString(article));
        }
        return new LoginMessage(-1, "Error");

    }

    @PostMapping("get_articles_range")
    public LoginMessage getRangeArticle(String userID,) {



        return new LoginMessage(-1, "Error");
    }

    @PostMapping("get_recommend_articles")
    public LoginMessage getRecommendArticles(String userID, int from, int to) {
        return new LoginMessage(-1, "error");
    }

}
