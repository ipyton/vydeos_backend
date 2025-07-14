package com.chen.blogbackend.services;

import com.chen.blogbackend.util.CloudflareCacheUtil;
import com.chen.blogbackend.util.ImageUtil;
import com.chen.blogbackend.util.RandomUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import io.minio.*;

import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@Service
public class PictureService {
    private static final Logger logger = LoggerFactory.getLogger(PictureService.class);

    @Autowired
    CqlSession sqlSessionFactory;

    @Autowired
    MinioClient fileClient;

    public boolean isPictureCapable(String mimeType) {
        logger.debug("Checking if mimeType is picture capable: {}", mimeType);

        if (mimeType == null) {
            logger.warn("MimeType is null, returning false");
            return false;
        }
        mimeType = mimeType.toLowerCase();

        boolean result = mimeType.startsWith("image/jpeg")
                || mimeType.startsWith("image/png")
                || mimeType.startsWith("image/gif")
                || mimeType.startsWith("image/bmp")
                || mimeType.startsWith("image/x-ms-bmp")
                || mimeType.startsWith("image/webp")
                || mimeType.startsWith("image/tiff")
                || mimeType.startsWith("image/x-portable-pixmap")    // PNM family: PPM/PGM/PBM/PNM
                || mimeType.startsWith("image/x-portable-anymap")
                || mimeType.startsWith("image/x-portable-graymap")
                || mimeType.startsWith("image/x-portable-bitmap");

        logger.debug("MimeType {} is picture capable: {}", mimeType, result);
        return result;
    }

    public boolean uploadAvatarPicture(String userEmail, MultipartFile file) throws IOException {
        logger.info("Starting avatar upload for user: {}", userEmail);

        String type = ImageUtil.getType(file.getInputStream());
        logger.debug("Detected file type: {}", type);

        boolean pictureCapable = isPictureCapable(type);
        if (!pictureCapable) {
            logger.warn("File type {} is not supported for user: {}", type, userEmail);
            return false;
        }

        String fileName = RandomUtil.getBase64(userEmail);
        logger.debug("Generated filename: {}", fileName);

        // 计算bucket名和文件路径
        String bucketName = "avatars";
        String filePath = fileName.substring(0, 2) + "/" + fileName;
        logger.debug("Target path: {}/{}", bucketName, filePath);

        try {
            // 检查bucket是否存在
            boolean found = fileClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                logger.info("Bucket {} does not exist, creating it", bucketName);
                // 如果bucket不存在，创建bucket
                fileClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("Successfully created bucket: {}", bucketName);
            }


            byte[] imageBytes = ImageUtil.processImage(file.getInputStream(),
                    0, 500, 512).readAllBytes();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            // 将文件上传到MinIO
            fileClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .stream(byteArrayInputStream, imageBytes.length, 5 * 1024 * 1024l)
                            .build()
            );
            String url ="https://apis.vydeo.xyz/java/account/getAvatar/" + "single" + "_"+ userEmail;
            ArrayList<String> urls = new ArrayList<>();
            urls.add(url);
            CloudflareCacheUtil.purgeByUrls(urls);



            logger.info("Avatar uploaded successfully for user: {} to path: {}/{}", userEmail, bucketName, filePath);

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Error occurred while uploading avatar for user: {}", userEmail, e);
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    public boolean uploadPostPicture(String articleID, MultipartFile file, int number) {
        logger.info("Starting post picture upload for article: {}, picture number: {}", articleID, number);

        String hash = RandomUtil.getBase64(articleID);
        String bucket = "articlePics";
        String objectName = articleID + number;

        logger.debug("Generated hash: {}, target object: {}", hash, objectName);

        try {
            boolean found = fileClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                logger.info("Bucket {} does not exist, creating it", bucket);
                fileClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                logger.info("Successfully created bucket: {}", bucket);
            }

            fileClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());

            logger.info("Post picture uploaded successfully for article: {}, picture number: {}, size: {} bytes",
                    articleID, number, file.getSize());

        } catch(Exception exception) {
            logger.error("Error occurred while uploading post picture for article: {}, picture number: {}",
                    articleID, number, exception);
            return false;
        }
        return true;
    }

    public StatObjectResponse getObjectStatus(String picAddress) {
        logger.debug("Getting object status for: {}", picAddress);

        StatObjectArgs args = StatObjectArgs.builder().bucket("articlePics").object(picAddress).build();
        StatObjectResponse response;
        try {
            response = fileClient.statObject(args);
            logger.debug("Successfully retrieved object status for: {}", picAddress);
        }
        catch (Exception exception) {
            logger.warn("Failed to get object status for: {}", picAddress, exception);
            return null;
        }
        return response;
    }

    public StreamingResponseBody getPostPictures(String path) {
        logger.info("Getting post picture for path: {}", path);

        StreamingResponseBody responseBody;
//        String objectName = articleID + Integer.toString(index);

        try {
            InputStream stream = fileClient.getObject(GetObjectArgs.builder()
                    .bucket("posts")
                    .object(path)
                    .build());

            responseBody = inputStreamConverter(stream);
            logger.info("Successfully retrieved post picture: {}", path);

        } catch (Exception e) {
            logger.error("Error occurred while getting post picture for path: {}", path, e);
            return null;
        }
        return responseBody;
    }

    private StreamingResponseBody inputStreamConverter(InputStream stream) {
        logger.debug("Converting InputStream to StreamingResponseBody");

        StreamingResponseBody responseBody;
        responseBody = outputStream -> {
            int numberToWrite = 0;
            byte[] data = new byte[1024];
            int totalBytes = 0;

            try {
                while ((numberToWrite = stream.read(data, 0, data.length)) != -1) {
                    outputStream.write(data, 0, numberToWrite);
                    totalBytes += numberToWrite;
                }
                logger.debug("Successfully streamed {} bytes", totalBytes);
            } catch (IOException e) {
                logger.error("Error occurred while streaming data", e);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    logger.warn("Error closing input stream", e);
                }
            }
        };
        return responseBody;
    }

    public InputStream getAvatar(String userEmail) {
        logger.info("Getting avatar for user: {}", userEmail);

        String fileName = RandomUtil.getBase64(userEmail);
        String filePath = fileName.substring(0, 2) + "/" + fileName;
        String bucketName = "avatars";

        logger.debug("Avatar path: {}/{}", bucketName, filePath);

        try {
            // 首先检查对象是否存在
            StatObjectArgs statArgs = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .build();

            fileClient.statObject(statArgs);
            logger.debug("Avatar exists for user: {}", userEmail);

            // 对象存在，获取输入流
            InputStream stream = fileClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .build());

            logger.info("Successfully retrieved avatar for user: {}", userEmail);
            return stream;

        } catch (Exception e) {
            logger.warn("Avatar not found or error occurred while getting avatar for user: {}", userEmail, e);
            return null;
        }
    }

    public void uploadChatPics(String fromId, String toId, String target) {
        logger.info("Chat picture upload method called - fromId: {}, toId: {}, target: {}", fromId, toId, target);
        logger.warn("uploadChatPics method is not implemented yet");
    }

    public void downloadChatPics(String fromId, String toId, String target) {
        logger.info("Chat picture download method called - fromId: {}, toId: {}, target: {}", fromId, toId, target);
        logger.warn("downloadChatPics method is not implemented yet");
    }

    public void deleteChatPics(String fromId, String toId, String target) {
        logger.info("Chat picture delete method called - fromId: {}, toId: {}, target: {}", fromId, toId, target);
        logger.warn("deleteChatPics method is not implemented yet");
    }

    public void deletePostPic(String userId, String postId, String targetId) {
        logger.info("Post picture delete method called - userId: {}, postId: {}, targetId: {}", userId, postId, targetId);
        logger.warn("deletePostPic method is not implemented yet");
    }

    public void deletePostPics(String userId, String postId) {
        logger.info("Post pictures delete method called - userId: {}, postId: {}", userId, postId);
        logger.warn("deletePostPics method is not implemented yet");
    }
}