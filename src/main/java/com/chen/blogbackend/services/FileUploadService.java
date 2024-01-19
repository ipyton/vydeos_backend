package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.VideoUploadingDao;
import com.chen.blogbackend.entities.UnfinishedUpload;
import com.chen.blogbackend.util.FileUtil;
import com.chen.blogbackend.util.StringUtil;
import com.chen.blogbackend.util.UnfinishedUploadCleaner;
import com.chen.blogbackend.util.VideoUtil;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


// server startup: looking for unloaded files in database, construct uploads map.
// upload failed: reload -> update upload map -> |
//                                              \/
// small files -> fully uploaded                 -> assemble -> convert -> store -> write information to db

@Service
public class FileUploadService {

    HashMap<String, UnfinishedUpload> uploads;
    static String sourceFilePath = VideoUtil.sourceFilePath;

    VideoUploadingDao dao;

    @Autowired
    MinioClient client;

    @Autowired
    Scheduler scheduler;

    @PostConstruct
    public void init() throws SchedulerException {
        //
        List<UnfinishedUpload> all = dao.getAll();
        for (UnfinishedUpload load: all
             ) {
            uploads.put(load.getFileHash(), load);
        }
        JobDetail build = JobBuilder.newJob(UnfinishedUploadCleaner.class).withIdentity("FileCleaner", "clean")
                .usingJobData("workingDir", "/files").build();
        build.getJobDataMap().put("dao", uploads);
        SimpleTrigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger").startNow().
                withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(30).repeatForever()).build();
        scheduler.scheduleJob(build, trigger);
        scheduler.start();
    }



    private boolean saveToOSS(UnfinishedUpload upload) {
        String bucket = "videos";
        try {
            boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            File indexFile = new File(sourceFilePath + upload.getFileHash() + ".m3u8");
            InputStream inputStream1 = new FileInputStream(indexFile);

            client.putObject(PutObjectArgs.builder().bucket(bucket).object(sourceFilePath + upload.getFileHash() + ".m3u8").stream(inputStream1, indexFile.length(), -1).build());
            for (int i = 0; i < upload.getTotal(); i ++) {
                File file = new File(sourceFilePath + upload.getFileHash() + "_" + i);
                InputStream inputStream = new FileInputStream(file);
                client.putObject(PutObjectArgs.builder().bucket(bucket).object(sourceFilePath + upload.getFileHash() + "_"+  i).stream(inputStream, file.length(), -1).build());
            }

        } catch(Exception exception) {
            System.out.println(exception);
            return false;
        }
        return true;

    }

    @PostConstruct
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
