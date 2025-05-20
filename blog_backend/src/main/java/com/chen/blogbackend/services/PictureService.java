package com.chen.blogbackend.services;

import com.chen.blogbackend.util.RandomUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import io.minio.*;

import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class PictureService {
    @Autowired
    CqlSession sqlSessionFactory;

    @Autowired
    MinioClient fileClient;

    public boolean uploadAvatarPicture(String userEmail, MultipartFile file) {
        // 使用userEmail生成哈希值来作为文件名
        System.out.println(userEmail);

        String fileName = RandomUtil.getBase64(userEmail);
        System.out.println(fileName);
        // 计算bucket名和文件路径
        String bucketName = "blogavatar";
        String filePath =  fileName.substring(0, 2) + "/" + fileName;

        try {
            // 检查bucket是否存在
            boolean found = fileClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                // 如果bucket不存在，创建bucket
                fileClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 将文件上传到MinIO
            fileClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            System.out.println("Error occurred: " + e);
            return false;
        }

        return true;
    }

    public boolean uploadPostPicture(String articleID, MultipartFile file, int number) {
        String hash = RandomUtil.getBase64(articleID);
        String bucket = "articlePics";
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
        StatObjectArgs args = StatObjectArgs.builder().bucket("articlePics").object(picAddress).build();
        StatObjectResponse response;
        try {
            response = fileClient.statObject(args);
        }
        catch (Exception exception) {
            return null;
        }
        return response;
    }


    public StreamingResponseBody getPostPicture(String userId,String articleID, int index) {
        StreamingResponseBody responseBody;
        try {
            InputStream stream = fileClient.getObject(GetObjectArgs.builder().bucket("articlePics").object(articleID + Integer.toString(index)).build());
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

    public InputStream getAvatar(String userEmail) {
        //StreamingResponseBody responseBody;
        System.out.println(userEmail);
        InputStream stream;
        String fileName = RandomUtil.getBase64(userEmail);
        System.out.println(fileName);
        String filePath = fileName.substring(0, 2) + "/" + fileName;
        System.out.println(filePath);
        try {
            stream = fileClient.getObject(GetObjectArgs.builder().bucket("blogavatar").object(filePath).build());
            //responseBody = inputStreamConverter(stream);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return stream;
    }

//    public ArrayList<String> getPostPictureAddress(String articleId) {
//        SqlSession session = sqlSessionFactory.openSession();
//        PictureMapper mapper = session.getMapper(PictureMapper.class);
//        int amount = mapper.getPictureAmountByArticleID(articleId);
//        String hash = RandomUtil.getHash(articleId);
//        ArrayList<String> result = new ArrayList<>();
//        for (int i = 0; i < amount; i ++) {
//            String address = hash + "_" + i;
//            result.add(address);
//        }
//        return result;
//    }

    public void uploadChatPics(String fromId, String toId, String target){

    }

    public void downloadChatPics(String fromId, String toId, String target) {


    }
    public void deleteChatPics(String fromId, String toId, String target) {


    }

    public void deletePostPic(String userId, String postId, String targetId) {

    }

    public void deletePostPics(String userId, String postId) {

    }



}
