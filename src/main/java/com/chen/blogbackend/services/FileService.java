package com.chen.blogbackend.services;

import com.alibaba.fastjson2.JSON;
import com.chen.blogbackend.DAO.VideoUploadingDao;
import com.chen.blogbackend.entities.EncodingRequest;
import com.chen.blogbackend.entities.FileUploadStatus;
import com.chen.blogbackend.entities.UnfinishedUpload;
import com.chen.blogbackend.mappers.FileServiceMapper;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.util.FileUtil;
import com.chen.blogbackend.util.UnfinishedUploadCleaner;
import com.chen.blogbackend.util.VideoUtil;
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
    PreparedStatement addAssets;
    PreparedStatement getUploadStatus;

    //PreparedStatement firstSlice;
    PreparedStatement addSlices;
    PreparedStatement updateStatus;
    PreparedStatement setUploadStatus;
    final Map<String, String> videoMimeTypes = new HashMap<>();

    TimeBasedGenerator timeBasedGenerator = Generators.timeBasedGenerator();

    @PostConstruct
    public void init() throws SchedulerException {
        //
//        List<UnfinishedUpload> all = dao.getAll();
//        for (UnfinishedUpload load: all
//             ) {
//            uploads.put(load.getFileHash(), load);
//        }
//        JobDetail build = JobBuilder.newJob(UnfinishedUploadCleaner.class).withIdentity("FileCleaner", "clean")
//                .usingJobData("workingDir", "/files").build();
//        build.getJobDataMap().put("dao", uploads);
//        SimpleTrigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger").startNow().
//                withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(30).repeatForever()).build();
//        scheduler.scheduleJob(build, trigger);
//        scheduler.start();
        uploadAvatar = cqlSession.prepare("update userinfo.user_information set avatar=? where user_id=?");
        addAssets = cqlSession.prepare("insert into files.assets (user_id, bucket, file) values (?,?,?) ");
        getUploadStatus = cqlSession.prepare("select * from files.file_upload_status where resource_id = ? and resource_type = ? and season_id = ? and episode = ?");
        updateStatus = cqlSession.prepare("update files.file_upload_status set status_code = ? where resource_id = ? and resource_type = ?  and season_id = ? and episode = ? and quality = ?");
        addSlices = cqlSession.prepare("update files.file_upload_status set current_slice = ? where resource_id = ? and resource_type = ? and season_id = ? and episode = ? and quality = ?");
        setUploadStatus = cqlSession.prepare("insert into files.file_upload_status (user_email, " +
                " resource_id, resource_type, whole_hash, file_name, total_slices, current_slice, size, " +
                "quality, status_code, format, season_id, episode) values (?,?,?,?,?,?,?,?,?,?,?, ?, ?)");
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

    }

    public LoginMessage setUploadStatus(String userEmail, String resourceId, String resourceType,
                                        String wholeMD5, long size, String filename, int totalSlice ,Short quality,
                                        String format, Integer seasonId, Integer episodeId) {
        if (resourceType.equals("video")) {
            seasonId = 0;
            episodeId = 0;
        }
        FileUploadStatus fileUploadStatus = getUploadStatus(resourceId, resourceType, seasonId, episodeId);
        if (null == fileUploadStatus) {
            ResultSet execute1 = cqlSession.execute(setUploadStatus.bind(userEmail, resourceId,
                    resourceType, wholeMD5, filename, totalSlice, 0,  size, quality,  0, format));
            List<Map.Entry<Node, Throwable>> errors = execute1.getExecutionInfo().getErrors();
            if (errors.isEmpty()) return new LoginMessage(1, "success");
            return new LoginMessage(-1, "failed");
        }

        return new LoginMessage(2, JSON.toJSONString(fileUploadStatus));
    }

    public FileUploadStatus getUploadStatus(String resourceId, String resourceType, Integer seasonId, Integer episodeId) {
        ResultSet execute = cqlSession.execute(getUploadStatus.bind( resourceId, resourceType, seasonId, episodeId));
        return FileServiceMapper.parseUploadStatus(execute);
    }



    public ResponseEntity<InputStreamResource> download(String bucket, String filename, MediaType type) {
        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(filename)
                            .build()
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(type)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    private boolean saveToOSS(UnfinishedUpload upload) {
        String bucket = "videos";
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            File indexFile = new File(sourceFilePath + upload.getFileHash() + ".m3u8");
            InputStream inputStream1 = new FileInputStream(indexFile);

            minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(sourceFilePath + upload.getFileHash() + ".m3u8").stream(inputStream1, indexFile.length(), -1).build());
            for (int i = 0; i < upload.getTotal(); i ++) {
                File file = new File(sourceFilePath + upload.getFileHash() + "_" + i);
                InputStream inputStream = new FileInputStream(file);
                minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(sourceFilePath + upload.getFileHash() + "_"+  i).stream(inputStream, file.length(), -1).build());
            }

        } catch(Exception exception) {
            System.out.println(exception);
            return false;
        }
        return true;
    }


    public ResponseEntity<String> uploadAvatar(String userEmail, MultipartFile file) {

        return uploadGeneral(file, "avatar", userEmail + ".jpg");
    }

    public ResponseEntity<String> uploadGeneral(MultipartFile file, String bucket, String filename) {
        try {
            // 创建存储桶（如果不存在）
            boolean isBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!isBucketExists) {
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

            return ResponseEntity.ok("File uploaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error occurred: " + e.getMessage());
        }
    }

    public ResponseEntity<String> uploadChatPics(String userEmail, MultipartFile file) {
        String uuid = timeBasedGenerator.generate().toString();
        cqlSession.execute(addAssets.bind(userEmail,uuid + ".jpg"));
        return ResponseEntity.ok(uuid + ".jpg");

    }

    public ResponseEntity<String> uploadPostPics(String userEmail, MultipartFile file) {
        String uuid = timeBasedGenerator.generate().toString();
        cqlSession.execute(addAssets.bind(userEmail,"postpics",uuid + ".jpg"));
        uploadGeneral(file,"postpics", uuid + ".jpg");
        return ResponseEntity.ok(uuid + ".jpg");
    }

    public ResponseEntity<String> uploadChatVideo(String userId, MultipartFile file) {
        String uuid = timeBasedGenerator.generate().toString();
        cqlSession.execute(addAssets.bind(userId,uuid));
        uploadGeneral(file,"chatvideo", uuid + ".jpg");
        return ResponseEntity.ok(uuid + ".jpg");
    }

    public ResponseEntity<String> uploadPostVideo(String userId, MultipartFile file) {
        String uuid = timeBasedGenerator.generate().toString();
        cqlSession.execute(addAssets.bind(userId,uuid));
        uploadGeneral(file,"postvideo", uuid + ".jpg");
        return ResponseEntity.ok(uuid + ".jpg");
    }

    public ResponseEntity<String> uploadChatVoice(String userId, MultipartFile file) {
        String uuid = timeBasedGenerator.generate().toString();
        cqlSession.execute(addAssets.bind(userId,uuid + ".jpg"));
        uploadGeneral(file,"chatvoice", uuid + ".jpg");
        return ResponseEntity.ok(uuid + ".jpg");
    }

    public ResponseEntity<String> uploadPostVoice(String userId, MultipartFile file) {
        String uuid = timeBasedGenerator.generate().toString();
        cqlSession.execute(addAssets.bind(userId,uuid + ".jpg"));
        uploadGeneral(file,"postvoice", uuid + ".jpg");
        return ResponseEntity.ok(uuid + ".jpg");
    }

    public boolean upload(UnfinishedUpload upload, String fragmentHash) throws Exception {
        // the file hash of the while file
        uploads.putIfAbsent(upload.getFileHash(), upload);
        String filePath = "";
        if (!FileUtil.calculateFileHash(filePath).equals(fragmentHash))  {
            //file is not consistent, retransmit
            return false;
        }
        upload.setCurrent(upload.getCurrent() + 1);
        if (upload.getCurrent() == upload.getTotal()) {
            VideoUtil.concat(upload);
            if (upload.getFileHash().equals(FileUtil.calculateFileHash(upload.getFileHash()))) {
                VideoUtil.convertToHLS(upload);
                saveToOSS(upload);
            }
            else {
                throw new Exception("file is not integral");
            }
        }
        dao.update(upload);

        return true;
    }

    public String getCheckSum(File file) throws NoSuchAlgorithmException, FileNotFoundException {
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
            throw new RuntimeException(e);
        }

        // 获取最终的 checksum
        byte[] bytes = digest.digest();

        // 将 byte[] 转为 Hex 字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }

    private boolean checkUploadFile(FileUploadStatus uploadStatus, Integer currentSlice, Integer totalSlice, MultipartFile file, String md5) throws IOException, NoSuchAlgorithmException {
        if (!Objects.equals(currentSlice, uploadStatus.getCurrentSlice())) {
            return false;
        }
        return checkSum(file, md5);
    }

    private boolean checkSum(MultipartFile file, String md5) throws IOException, NoSuchAlgorithmException {
        InputStream inputStream = file.getInputStream();
        boolean b = checkSum(inputStream, md5);
        inputStream.close();
        return b;

    }

    private boolean checkSum(String path, String md5) throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(path);
        boolean b = checkSum(fis, md5);
        fis.close();
        return b;
    }

    private boolean checkSum(InputStream inputStream, String md5) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        String abstraction = hexString.toString();
        System.out.println(abstraction);
        System.out.println(md5);
        return abstraction.equals(md5);
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
        FileUploadStatus uploadStatus = getUploadStatus(resourceId, type, season, episode);
        return videoMimeTypes.get(uploadStatus.getFormat());
    }


    //fileUploadStage1
    //fileUploadStage2
    //fileUploadStage3
    public int uploadFile(MultipartFile file, String type, Integer currentSlice,
                                       String resourceId, String userEmail, String checkSum, Integer seasonId, Integer episode) throws Exception {
        if (file.isEmpty()) {
            System.out.println("file is empty!");
            return -1;
        }
        FileUploadStatus uploadStatus = getUploadStatus(resourceId, type,seasonId, episode);
        if (uploadStatus == null) {
            return -1;
        }
        if (uploadStatus.currentSlice > currentSlice) {
            if (!uploadStatus.currentSlice.equals(uploadStatus.totalSlices)) {
                System.out.println(uploadStatus  + " + " + uploadStatus.totalSlices);
                return 0;
            }
        }
        else if (Objects.equals(uploadStatus.currentSlice, currentSlice)) {
            // 检查该分片是否正常
            boolean isValid = checkUploadFile(uploadStatus, currentSlice, uploadStatus.getTotalSlices(), file, checkSum);
            if (!isValid) {
                System.out.println("wrong order or checksum!");
                return -1;
            }
        } else if (!uploadStatus.currentSlice.equals(uploadStatus.totalSlices)) return -1;
        String base = System.getProperty("user.home") + "/tmp/" +type + "_" + resourceId;
        if (Objects.equals(uploadStatus.currentSlice, currentSlice)) {
            File folder = new File(base);

            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    System.out.println("文件夹创建成功：" + folder.getAbsolutePath());
                } else {
                    System.out.println("创建文件夹失败！" + folder.getAbsolutePath());
                    return -1;
                }
            } else {
                System.out.println("文件夹已存在！");
            }
            File fileToWrite = new File( base + "/" + currentSlice + "_" + uploadStatus.getTotalSlices());
            file.transferTo(fileToWrite);
            cqlSession.execute(addSlices.bind(currentSlice + 1 ,resourceId, type));
        }


        if (currentSlice == 0 && !uploadStatus.currentSlice.equals(uploadStatus.totalSlices)) {
            cqlSession.execute(updateStatus.bind(1,resourceId, type));
            cqlSession.execute(addSlices.bind(currentSlice + 1 ,resourceId, type));
        }
        //额外处理
        else if (currentSlice.equals(uploadStatus.getTotalSlices() - 1) || Objects.equals(uploadStatus.currentSlice, uploadStatus.totalSlices)) {
            //最后一个分段
            cqlSession.execute(updateStatus.bind(2, resourceId, type));
            executor.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {

                    String path =  base + "/" + resourceId.hashCode() + getSuffix(resourceId, type,seasonId,episode);

                    try (FileOutputStream fis = new FileOutputStream(path)){
                        System.out.println("start");
                        for (int i = 0; i < uploadStatus.getTotalSlices(); i ++) {

                            try (FileInputStream subFiles = new FileInputStream( base + "/" + i + "_" +
                                    uploadStatus.getTotalSlices())) {
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = subFiles.read(buffer)) != -1) {
                                    fis.write(buffer, 0, bytesRead);
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                        boolean b = checkSum(path, uploadStatus.getWholeHash());
                        if (!b) {
                            throw new Exception("检验失败");
                        }
                        for (int i = 0; i < uploadStatus.getTotalSlices(); i ++) {
                            File file = new File( base + "/" + i + "_" + uploadStatus.getTotalSlices()); // 指定文件路径

                            if (file.delete()) {
                                System.out.println("文件已成功删除");
                            } else {
                                System.out.println("文件删除失败");
                            }
                        }
                        producer.send(new ProducerRecord<>("fileUploadStage1", type + "_" +resourceId,
                                JSON.toJSONString(new EncodingRequest( base +  "/"
                                + resourceId.hashCode() + getSuffix(resourceId, type,seasonId,episode),
                                        System.getProperty("user.home") + "/tmp/encoded/" +  type + "_" + (resourceId) + "/",
                                        "",
                                        "",resourceId, type))), (metadata, exception) -> {
                            if (exception == null) {
                                System.out.printf("消息发送成功: topic=%s, partition=%d, offset=%d, key=%s\n",
                                        metadata.topic(), metadata.partition(), metadata.offset(),resourceId + "_" +type);
                            } else {
                                System.err.println("消息发送失败: " + exception.getMessage());
                            }
                        });
                        cqlSession.execute(updateStatus.bind(3,resourceId, type));
                    } catch (Exception e) {
                        e.printStackTrace();
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
