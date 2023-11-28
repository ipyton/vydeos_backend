package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Article;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.print.Doc;

@Component
public class MongoArticleMapper {

    @Autowired
    MongoClient client;
    MongoDatabase db;
    MongoCollection<Document> doc;

    public void init() {
        db = client.getDatabase("blog");
        doc = db.getCollection("article");
    }


    public Document convertToDocument(Article article) {
        if (null == article) return null;
        Document doc = new Document();
        doc.append("articleID",article.getArticleID());
        doc.append("userID", article.getUserID());
        doc.append("content",article.getContent());
        doc.append("comments", article.getCommentUrl());
        doc.append("likes",article.getLikes());
        doc.append("lastEdit", article.getLastEdit());
        doc.append("pictures",article.getPictureUrl());
        return doc;
    }

    public boolean insert(Article article){
        doc.insertOne(convertToDocument(article));
        return true;
    }

    public Article getFromTo(String userEmail, int from, int to){
        return new Article();
    }

    public Article getByID(String articleID) {

        return new Article();
    }

    public boolean modify(Article article) {
        return true;
    }

}
