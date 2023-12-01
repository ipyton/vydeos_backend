package com.chen.blogbackend.services;

import com.chen.blogbackend.Util.StringUtil;
import com.chen.blogbackend.mappers.AccountMapper;
import com.chen.blogbackend.mappers.PictureMapper;
import io.minio.*;
import io.minio.errors.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@Service
public class PictureService {
    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    MinioClient fileClient;

    public boolean uploadAvatarPicture(String userEmail, MultipartFile file) {
        String hash = StringUtil.getHash(userEmail);
        System.out.println(hash);
        String bucket = "avatar";
        try {
            boolean found = fileClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                fileClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }

            fileClient.putObject(PutObjectArgs.builder().bucket(bucket).object(userEmail).stream(file.getInputStream(), file.getSize(), -1).build());
        } catch(Exception exception) {
            System.out.println(exception);
            return false;
        }

        return true;
    }

    public boolean uploadArticlePicture(String articleID, MultipartFile file, int number) {
        String hash = StringUtil.getHash(articleID);
        String bucket = "articlePics/" + hash;
        try {
            boolean found = fileClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                fileClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            fileClient.putObject(PutObjectArgs.builder().bucket(bucket).object(articleID + number).stream(file.getInputStream(), file.getSize(), -1).build());
        } catch(Exception exception) {
            System.out.println(exception);
            return false;
        }
        return true;
    }

    public StatObjectResponse getObjectStatus(String picAddress)  {
        StatObjectArgs args = StatObjectArgs.builder().bucket("articlePics/").object(picAddress).build();
        StatObjectResponse response;
        try {
            response = fileClient.statObject(args);
        }
        catch (Exception exception) {
            return null;
        }
        return response;
    }


    public StreamingResponseBody getPicture(String picAddress) {
        StreamingResponseBody responseBody;
        try {
            InputStream stream = fileClient.getObject(GetObjectArgs.builder().bucket("articlePics/").object(picAddress).build());
            responseBody = inputStreamConverter(stream);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return responseBody;
    }

    private StreamingResponseBody inputStreamConverter(InputStream stream) {
        StreamingResponseBody responseBody;
        responseBody = outputStream ->{
            int numberToWrite = 0;
            byte[] data = new byte[1024];
            while ((numberToWrite = stream.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, numberToWrite);
            }
        };
        return responseBody;

    }

    public StreamingResponseBody getAvatar(String userEmail) {
        StreamingResponseBody responseBody;
        String hash = StringUtil.getHash(userEmail);
        try {
            InputStream stream = fileClient.getObject(GetObjectArgs.builder().bucket("avatar/" + hash).object(userEmail).build());
            responseBody = inputStreamConverter(stream);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return responseBody;
    }

    public ArrayList<String> getArticlePictureAddress(String articleId) {
        SqlSession session = sqlSessionFactory.openSession();
        PictureMapper mapper = session.getMapper(PictureMapper.class);
        int amount = mapper.getPictureAmountByArticleID(articleId);
        String hash = StringUtil.getHash(articleId);
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < amount; i ++) {
            String address = hash + "_" + i;
            result.add(address);
        }
        return result;
    }
}
