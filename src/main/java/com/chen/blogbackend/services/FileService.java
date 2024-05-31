package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.VideoUploadingDao;
import com.chen.blogbackend.entities.UnfinishedUpload;
import com.chen.blogbackend.util.FileUtil;
import com.chen.blogbackend.util.UnfinishedUploadCleaner;
import com.chen.blogbackend.util.VideoUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import okhttp3.MultipartBody;
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
import java.util.HashMap;
import java.util.List;


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
//    @Autowired
//    Scheduler scheduler;

    PreparedStatement uploadAvatar;
    PreparedStatement addAssets;
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

}
