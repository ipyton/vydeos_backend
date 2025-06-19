package com.chen.blogbackend.services;

import com.alibaba.fastjson2.JSON;
import com.chen.blogbackend.DAO.VideoUploadingDao;
import com.chen.blogbackend.entities.EncodingRequest;
import com.chen.blogbackend.entities.FileUploadStatus;
import com.chen.blogbackend.entities.UnfinishedUpload;
import com.chen.blogbackend.experiments.XXHashComputer;
import com.chen.blogbackend.mappers.FileServiceMapper;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.util.*;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.checkerframework.checker.units.qual.A;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


// server startup: looking for unloaded files in database, construct uploads map.
// upload failed: reload -> update upload map -> |
//                                               \/
// small files -> fully uploaded                 -> assemble -> convert -> store -> write information to db

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    HashMap<String, UnfinishedUpload> uploads;
    static String sourceFilePath = VideoUtil.sourceFilePath;

    VideoUploadingDao dao;

    @Autowired
    MinioClient minioClient;

    @Autowired
    CqlSession cqlSession;

    @Autowired
    Producer<String, String> producer;


//    @Resource(name = "movieProducer")
//    Producer<String, String> producer;
//    @Autowired
//    Scheduler scheduler;

    ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 10, 1000,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(10), new ThreadPoolExecutor.AbortPolicy());


    PreparedStatement uploadAvatar;
    PreparedStatement getUploadStatus;

    //PreparedStatement firstSlice;
    PreparedStatement addSlices;
    PreparedStatement updateStatus;
    PreparedStatement setUploadStatus;

    PreparedStatement addTempPostImage;

    final Map<String, String> videoMimeTypes = new HashMap<>();

    TimeBasedGenerator timeBasedGenerator = Generators.timeBasedGenerator();

    @PostConstruct
    public void init() throws SchedulerException {
        logger.info("Initializing FileService...");

        try {
            // Initialize prepared statements
            uploadAvatar = cqlSession.prepare("update userinfo.user_information set avatar=? where user_id=?");
            getUploadStatus = cqlSession.prepare("select * from files.file_upload_status where resource_id = ? and resource_type = ? and season_id = ? and episode = ?");
            updateStatus = cqlSession.prepare("update files.file_upload_status set status_code = ? where resource_id = ? and resource_type = ?  and season_id = ? and episode = ? and quality = ?");
            addSlices = cqlSession.prepare("update files.file_upload_status set current_slice = ? where resource_id = ? and resource_type = ? and season_id = ? and episode = ? and quality = ?");
            setUploadStatus = cqlSession.prepare("insert into files.file_upload_status (user_email, " +
                    " resource_id, resource_type, whole_hash, file_name, total_slices, current_slice, size, " +
                    "quality, status_code, format, season_id, episode) values (?,?,?,?,?,?,?,?,?,?,?, ?, ?)");

            logger.debug("Prepared statements initialized successfully");
            addTempPostImage = cqlSession.prepare("insert into posts.temporary_post_pics " +
                    "(author_id, path, timestamp) values(?, ?, ?)");
            // Initialize video MIME types
            videoMimeTypes.put("video/mp4", ".mp4");
            videoMimeTypes.put("video/webm", ".webm");
            videoMimeTypes.put("video/ogg", ".ogv");
            videoMimeTypes.put("video/quicktime", ".mov");
            videoMimeTypes.put("video/x-msvideo", ".avi");
            videoMimeTypes.put("video/x-flv", ".flv");
            videoMimeTypes.put("video/mpeg", ".mpeg");
            videoMimeTypes.put("video/3gpp", ".3gp");
            videoMimeTypes.put("video/3gpp2", ".3g2");
            videoMimeTypes.put("video/x-matroska", ".mkv");
            videoMimeTypes.put("video/x-ms-wmv", ".wmv");

            logger.info("FileService initialization completed successfully. Supported video formats: {}", videoMimeTypes.size());

        } catch (Exception e) {
            logger.error("Failed to initialize FileService", e);
            throw e;
        }
    }

    public LoginMessage setUploadStatus(String userEmail, String resourceId, String resourceType,
                                        String wholeMD5, long size, String filename, int totalSlice ,Short quality,
                                        String format, Integer seasonId, Integer episodeId) {
        logger.info("Setting upload status for resource: {} type: {} user: {}", resourceId, resourceType, userEmail);

        if (resourceType.equals("video")) {
            seasonId = 0;
            episodeId = 0;
            logger.debug("Resource type is video, setting seasonId and episodeId to 0");
        }

        FileUploadStatus fileUploadStatus = getUploadStatus(resourceId, resourceType, seasonId, episodeId);
        if (null == fileUploadStatus) {
            logger.debug("No existing upload status found, creating new one");
            ResultSet execute1 = cqlSession.execute(setUploadStatus.bind(userEmail, resourceId,
                    resourceType, wholeMD5, filename, totalSlice, 0,  size, quality,  0, format,seasonId,episodeId));
            List<Map.Entry<Node, Throwable>> errors = execute1.getExecutionInfo().getErrors();
            if (errors.isEmpty()) {
                logger.info("Upload status created successfully for resource: {}", resourceId);
                return new LoginMessage(1, "success");
            } else {
                logger.error("Failed to create upload status for resource: {}. Errors: {}", resourceId, errors);
                return new LoginMessage(-1, "failed");
            }
        }

        logger.info("Upload status already exists for resource: {}", resourceId);
        return new LoginMessage(2, JSON.toJSONString(fileUploadStatus));
    }

    public FileUploadStatus getUploadStatus(String resourceId, String resourceType, Integer seasonId, Integer episodeId) {
        logger.debug("Getting upload status for resource: {} type: {} season: {} episode: {}",
                resourceId, resourceType, seasonId, episodeId);

        ResultSet execute = cqlSession.execute(getUploadStatus.bind( resourceId, resourceType, seasonId, episodeId));
        FileUploadStatus status = FileServiceMapper.parseUploadStatus(execute);

        if (status != null) {
            logger.debug("Found upload status for resource: {}", resourceId);
        } else {
            logger.debug("No upload status found for resource: {}", resourceId);
        }

        return status;
    }

    public ResponseEntity<InputStreamResource> download(String bucket, String filename, MediaType type) {
        logger.info("Downloading file: {} from bucket: {}", filename, bucket);

        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(filename)
                            .build()
            );

            logger.info("File downloaded successfully: {}", filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(type)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            logger.error("Failed to download file: {} from bucket: {}", filename, bucket, e);
            return ResponseEntity.status(404).body(null);
        }
    }

    private boolean saveToOSS(UnfinishedUpload upload) {
        String bucket = "videos";
        logger.info("Saving to OSS - fileHash: {} bucket: {}", upload.getFileHash(), bucket);

        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                logger.info("Bucket {} does not exist, creating it", bucket);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }

            // Upload index file
            File indexFile = new File(sourceFilePath + upload.getFileHash() + ".m3u8");
            logger.debug("Uploading index file: {}", indexFile.getPath());
            InputStream inputStream1 = new FileInputStream(indexFile);
            minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(sourceFilePath + upload.getFileHash() + ".m3u8").stream(inputStream1, indexFile.length(), -1).build());

            // Upload segment files
            for (int i = 0; i < upload.getTotal(); i ++) {
                File file = new File(sourceFilePath + upload.getFileHash() + "_" + i);
                logger.debug("Uploading segment file {}: {}", i, file.getPath());
                InputStream inputStream = new FileInputStream(file);
                minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(sourceFilePath + upload.getFileHash() + "_"+  i).stream(inputStream, file.length(), -1).build());
            }

            logger.info("Successfully saved to OSS - fileHash: {}", upload.getFileHash());
        } catch(Exception exception) {
            logger.error("Failed to save to OSS - fileHash: {}", upload.getFileHash(), exception);
            return false;
        }
        return true;
    }

    public ResponseEntity<String> uploadAvatar(String userEmail, MultipartFile file) {
        logger.info("Uploading avatar for user: {}", userEmail);
        return uploadGeneral(file, "avatar", userEmail + ".jpg");
    }

    public ResponseEntity<String> uploadGeneral(MultipartFile file, String bucket, String filename) {
        logger.info("Uploading file: {} to bucket: {}", filename, bucket);

        try {
            // 创建存储桶（如果不存在）
            boolean isBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!isBucketExists) {
                logger.info("Bucket {} does not exist, creating it", bucket);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            logger.info("File uploaded successfully: {} to bucket: {}", filename, bucket);
            return ResponseEntity.ok("File uploaded successfully.");
        } catch (Exception e) {
            logger.error("Error uploading file: {} to bucket: {}", filename, bucket, e);
            return ResponseEntity.status(500).body("Error occurred: " + e.getMessage());
        }
    }

    public void uploadGeneral(ByteArrayInputStream inputStream, long size,
                                                String contentType, String bucket, String filename) throws Exception {
        logger.info("Uploading file (from stream): {} to bucket: {}", filename, bucket);

        try {
            // 创建存储桶（如果不存在）
            boolean isBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!isBucketExists) {
                logger.info("Bucket {} does not exist, creating it", bucket);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(filename)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );

            logger.info("File uploaded successfully (from stream): {} to bucket: {}", filename, bucket);
        } catch (Exception e) {
            logger.error("Error uploading file (from stream): {} to bucket: {}", filename, bucket, e);
            throw new Exception("Error occurred: " + e.getMessage());
        }
    }


    public ResponseEntity<String> uploadChatPics(String userEmail, MultipartFile file) {
        logger.info("Uploading chat picture for user: {}", userEmail);
        String uuid = timeBasedGenerator.generate().toString();
        cqlSession.execute(addTempPostImage.bind(userEmail,uuid + ".jpg"));
        logger.debug("Generated UUID for chat picture: {}", uuid);
        return ResponseEntity.ok(uuid + ".jpg");
    }

    public String uploadPostPics(String userEmail, MultipartFile file) throws Exception {
        logger.info("Uploading post picture for user: {}", userEmail);
        Long uuid = RandomUtil.generateTimeBasedRandomLong(userEmail);
        String path = uuid.toString().substring(0,3) + "/"+ uuid + ".jpg";

        cqlSession.execute(addTempPostImage.bind(userEmail,path, Instant.now()));
        ByteArrayInputStream byteArrayInputStream = ImageUtil.processImage(file.getInputStream(),200,500, 0);
        uploadGeneral(byteArrayInputStream,(long)byteArrayInputStream.available(),"image/jpeg","posts",  path);
        logger.debug("Generated UUID for post picture: {}", uuid);
        return path;
    }

    public ResponseEntity<String> uploadChatVideo(String userId, MultipartFile file) {
        logger.info("Uploading chat video for user: {}", userId);
        String uuid = timeBasedGenerator.generate().toString();
        cqlSession.execute(addTempPostImage.bind(userId,uuid));
        uploadGeneral(file,"chatvideo", uuid + ".jpg");
        logger.debug("Generated UUID for chat video: {}", uuid);
        return ResponseEntity.ok(uuid + ".jpg");
    }

    public ResponseEntity<String> uploadPostVideo(String userId, MultipartFile file) {
        logger.info("Uploading post video for user: {}", userId);
        String uuid = timeBasedGenerator.generate().toString();
        cqlSession.execute(addTempPostImage.bind(userId,uuid));
        uploadGeneral(file,"postvideo", uuid + ".jpg");
        logger.debug("Generated UUID for post video: {}", uuid);
        return ResponseEntity.ok(uuid + ".jpg");
    }

    public ResponseEntity<String> uploadChatVoice(String userId, MultipartFile file) {
        logger.info("Uploading chat voice for user: {}", userId);
        String uuid = timeBasedGenerator.generate().toString();
        cqlSession.execute(addTempPostImage.bind(userId,uuid + ".jpg"));
        uploadGeneral(file,"chatvoice", uuid + ".jpg");
        logger.debug("Generated UUID for chat voice: {}", uuid);
        return ResponseEntity.ok(uuid + ".jpg");
    }

    public ResponseEntity<String> uploadPostVoice(String userId, MultipartFile file) {
        logger.info("Uploading post voice for user: {}", userId);
        String uuid = timeBasedGenerator.generate().toString();
        cqlSession.execute(addTempPostImage.bind(userId,uuid + ".jpg"));
        uploadGeneral(file,"postvoice", uuid + ".jpg");
        logger.debug("Generated UUID for post voice: {}", uuid);
        return ResponseEntity.ok(uuid + ".jpg");
    }

    public boolean upload(UnfinishedUpload upload, String fragmentHash) throws Exception {
        logger.info("Processing upload for fileHash: {}", upload.getFileHash());

        // the file hash of the while file
        uploads.putIfAbsent(upload.getFileHash(), upload);
        String filePath = "";
        if (!FileUtil.calculateFileHash(filePath).equals(fragmentHash))  {
            //file is not consistent, retransmit
            logger.warn("File hash mismatch for upload: {} expected: {} actual: {}",
                    upload.getFileHash(), fragmentHash, FileUtil.calculateFileHash(filePath));
            return false;
        }

        upload.setCurrent(upload.getCurrent() + 1);
        logger.debug("Upload progress: {}/{} for fileHash: {}",
                upload.getCurrent(), upload.getTotal(), upload.getFileHash());

        if (upload.getCurrent() == upload.getTotal()) {
            logger.info("Upload completed, starting post-processing for fileHash: {}", upload.getFileHash());
            VideoUtil.concat(upload);
            if (upload.getFileHash().equals(FileUtil.calculateFileHash(upload.getFileHash()))) {
                logger.info("File integrity verified, converting to HLS for fileHash: {}", upload.getFileHash());
                VideoUtil.convertToHLS(upload);
                saveToOSS(upload);
            }
            else {
                logger.error("File integrity check failed for fileHash: {}", upload.getFileHash());
                throw new Exception("file is not integral");
            }
        }
        dao.update(upload);

        return true;
    }

    public String getCheckSum(File file) throws NoSuchAlgorithmException, FileNotFoundException {
        logger.debug("Calculating checksum for file: {}", file.getPath());

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // 创建输入流读取文件
        try (InputStream is = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;

            // 读取文件并更新 MessageDigest
            while ((bytesCount = is.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        } catch (IOException e) {
            logger.error("Error reading file for checksum calculation: {}", file.getPath(), e);
            throw new RuntimeException(e);
        }

        // 获取最终的 checksum
        byte[] bytes = digest.digest();

        // 将 byte[] 转为 Hex 字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }

        String checksum = hexString.toString();
        logger.debug("Checksum calculated for file: {} checksum: {}", file.getPath(), checksum);
        return checksum;
    }

    private boolean checkUploadFile(FileUploadStatus uploadStatus, Integer currentSlice, Integer totalSlice, MultipartFile file, String hash) throws IOException, NoSuchAlgorithmException {
        logger.debug("Checking upload file - currentSlice: {} expectedSlice: {}", currentSlice, uploadStatus.getCurrentSlice());

        if (!Objects.equals(currentSlice, uploadStatus.getCurrentSlice())) {
            logger.warn("Slice order mismatch - expected: {} received: {}", uploadStatus.getCurrentSlice(), currentSlice);
            return false;
        }
        return checkSum(file, hash);
    }

    private boolean checkSum(MultipartFile file, String hash) throws IOException, NoSuchAlgorithmException {
        logger.debug("Verifying checksum for uploaded file");
        InputStream inputStream = file.getInputStream();
        boolean b = checkSum(inputStream, hash);
        inputStream.close();
        return b;
    }

    private boolean checkSum(String path, String hash) throws IOException, NoSuchAlgorithmException {
        logger.debug("Verifying checksum for file: {}", path);
        FileInputStream fis = new FileInputStream(path);
        boolean b = checkSum(fis, hash);
        fis.close();
        return b;
    }

    private boolean checkSum(InputStream inputStream, String hash) throws NoSuchAlgorithmException, IOException {
        String s = XXHashComputer.computeHash(inputStream, -1);
        boolean isValid = s.equals(hash);
        logger.debug("Checksum verification result: {} expected: {} actual: {}", isValid, hash, s);
        return isValid;
    }

    // This is a tool class used for adapt windows folder
    public static String extractBeforeQuestionMark(String input) {
        // 查找问号的位置
        int questionMarkIndex = input.indexOf('?');

        // 如果找到了问号，返回问号前的部分；如果没有找到问号，返回原字符串
        if (questionMarkIndex != -1) {
            return input.substring(0, questionMarkIndex);
        } else {
            return input; // 如果没有问号，返回整个字符串
        }
    }

    private String getSuffix(String resourceId, String type, Integer season, Integer episode) {
        logger.debug("Getting file suffix for resource: {} type: {}", resourceId, type);
        FileUploadStatus uploadStatus = getUploadStatus(resourceId, type, season, episode);
        return videoMimeTypes.get(uploadStatus.getFormat());
    }

    //fileUploadStage1
    //fileUploadStage2
    //fileUploadStage3

    // 1. ~/tmp/movie_xxx_{season}_{episode}/1,2,3,4 segments
    // 2
    public int uploadFile(MultipartFile file, String type, Integer currentSlice,
                          String resourceId, String userEmail, String checkSum, Integer seasonId, Integer episode) throws Exception {
        logger.info("Uploading file slice - resource: {} type: {} slice: {}/{} user: {}",
                resourceId, type, currentSlice, "unknown", userEmail);

        if (file.isEmpty()) {
            logger.warn("Upload failed - file is empty for resource: {}", resourceId);
            return -1;
        }

        FileUploadStatus uploadStatus = getUploadStatus(resourceId, type, seasonId, episode);
        if (uploadStatus == null) {
            logger.error("Upload status not found for resource: {} type: {}", resourceId, type);
            return -1;
        }

        logger.debug("Current upload status - resource: {} currentSlice: {} totalSlices: {}",
                resourceId, uploadStatus.currentSlice, uploadStatus.totalSlices);

        if (uploadStatus.currentSlice > currentSlice) {
            if (!uploadStatus.currentSlice.equals(uploadStatus.totalSlices)) {
                logger.info("Slice already uploaded - resource: {} slice: {}", resourceId, currentSlice);
                return 0;
            }
        }
        else if (Objects.equals(uploadStatus.currentSlice, currentSlice)) {
            // 检查该分片是否正常
            boolean isValid = checkUploadFile(uploadStatus, currentSlice, uploadStatus.getTotalSlices(), file, checkSum);
            if (!isValid) {
                logger.error("Upload validation failed - wrong order or checksum for resource: {} slice: {}",
                        resourceId, currentSlice);
                return -1;
            }
        } else if (!uploadStatus.currentSlice.equals(uploadStatus.totalSlices)) {
            logger.warn("Invalid slice order for resource: {} expected: {} received: {}",
                    resourceId, uploadStatus.currentSlice, currentSlice);
            return -1;
        }

        String base = System.getProperty("user.home") + "/tmp/" +type + "_" + resourceId + "_" + seasonId + "_" + episode;
        logger.debug("Working directory: {}", base);

        if (Objects.equals(uploadStatus.currentSlice, currentSlice)) {
            File folder = new File(base);

            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    logger.info("Created upload directory: {}", folder.getAbsolutePath());
                } else {
                    logger.error("Failed to create upload directory: {}", folder.getAbsolutePath());
                    return -1;
                }
            } else {
                logger.debug("Upload directory already exists: {}", folder.getAbsolutePath());
            }

            File fileToWrite = new File( base + "/" + currentSlice + "_" + uploadStatus.getTotalSlices());
            file.transferTo(fileToWrite);
            logger.debug("Saved file slice: {}", fileToWrite.getPath());

            cqlSession.execute(addSlices.bind(currentSlice + 1 ,resourceId, type, seasonId, episode, (short) 1));
            logger.debug("Updated slice progress to: {}", currentSlice + 1);
        }

        if (currentSlice == 0 && !uploadStatus.currentSlice.equals(uploadStatus.totalSlices)) {
            logger.info("Starting upload process for resource: {}", resourceId);
            cqlSession.execute(updateStatus.bind(1,resourceId, type,seasonId,episode, (short) 1));
            cqlSession.execute(addSlices.bind(currentSlice + 1 ,resourceId, type,seasonId,episode,(short)1));
        }
        //额外处理
        else if (currentSlice.equals(uploadStatus.getTotalSlices() - 1) || Objects.equals(uploadStatus.currentSlice, uploadStatus.totalSlices)) {
            //最后一个分段
            logger.info("Processing final slice for resource: {}, starting file assembly", resourceId);
            cqlSession.execute(updateStatus.bind(2, resourceId, type,seasonId,episode, (short) 1));

            executor.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    logger.info("Starting background file assembly for resource: {}", resourceId);

                    String path =  base + "/" + resourceId.hashCode() + getSuffix(resourceId, type,seasonId,episode);

                    try (FileOutputStream fis = new FileOutputStream(path)){
                        logger.debug("Assembling file segments into: {}", path);

                        for (int i = 0; i < uploadStatus.getTotalSlices(); i ++) {
                            String segmentPath = base + "/" + i + "_" + uploadStatus.getTotalSlices();
                            logger.debug("Processing segment {}: {}", i, segmentPath);

                            try (FileInputStream subFiles = new FileInputStream(segmentPath)) {
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = subFiles.read(buffer)) != -1) {
                                    fis.write(buffer, 0, bytesRead);
                                }
                            }
                            catch (Exception e) {
                                logger.error("Error processing segment {}: {}", i, segmentPath, e);
                                throw new RuntimeException(e);
                            }
                        }

                        // Verify assembled file
                        boolean b = checkSum(path, uploadStatus.getWholeHash());
                        if (!b) {
                            logger.error("File integrity check failed for assembled file: {}", path);
                            throw new Exception("检验失败");
                        }
                        logger.info("File integrity verified for: {}", path);

                        // Clean up segment files
                        for (int i = 0; i < uploadStatus.getTotalSlices(); i ++) {
                            File file = new File( base + "/" + i + "_" + uploadStatus.getTotalSlices());
                            if (file.delete()) {
                                logger.debug("Deleted segment file: {}", file.getPath());
                            } else {
                                logger.warn("Failed to delete segment file: {}", file.getPath());
                            }
                        }

                        // Send to encoding queue
                        String encodingRequestJson = JSON.toJSONString(new EncodingRequest( base +  "/"
                                + resourceId.hashCode() + getSuffix(resourceId, type,seasonId,episode),
                                System.getProperty("user.home") + "/tmp/encoded/" +  type + "_" + (resourceId)  + "_" + seasonId + "_"+ episode + "/",
                                "",
                                "",resourceId, type,seasonId,episode));

                        producer.send(new ProducerRecord<>("fileUploadStage1", type + "_" +resourceId, encodingRequestJson),
                                (metadata, exception) -> {
                                    if (exception == null) {
                                        logger.info("Encoding request sent successfully - topic: {} partition: {} offset: {} key: {}",
                                                metadata.topic(), metadata.partition(), metadata.offset(),
                                                resourceId + "_" +type + "_" + seasonId + "_"+ episode + "/");
                                    } else {
                                        logger.error("Failed to send encoding request for resource: {}", resourceId, exception);
                                    }
                                });

                        cqlSession.execute(updateStatus.bind(3,resourceId, type, seasonId, episode,(short)1));
                        logger.info("File assembly completed successfully for resource: {}", resourceId);

                    } catch (Exception e) {
                        logger.error("Error during file assembly for resource: {}", resourceId, e);
                        throw new Exception(e);
                    }
                    return null;
                }
            });
            return 1;
        }

        return 0;
    }
}