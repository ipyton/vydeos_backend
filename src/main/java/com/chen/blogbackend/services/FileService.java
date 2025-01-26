package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.VideoUploadingDao;
import com.chen.blogbackend.entities.FileUploadStatus;
import com.chen.blogbackend.entities.UnfinishedUpload;
import com.chen.blogbackend.mappers.FileServiceMapper;
import com.chen.blogbackend.util.FileUtil;
import com.chen.blogbackend.util.UnfinishedUploadCleaner;
import com.chen.blogbackend.util.VideoUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import okhttp3.MultipartBody;
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
//    @Autowired
//    Scheduler scheduler;

    ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 10, 1000,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(10), new ThreadPoolExecutor.AbortPolicy());


    PreparedStatement uploadAvatar;
    PreparedStatement addAssets;
    PreparedStatement getUploadStatus;

    //PreparedStatement firstSlice;
    PreparedStatement addSlices;
    PreparedStatement lastSlice;


    TimeBasedGenerator timeBasedGenerator = Generators.timeBasedGenerator();
    private CqlSession setScyllaSession;

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
        getUploadStatus = cqlSession.prepare("select * from files.file_upload_status where resource_id = ? and resource_type = ?");
        addSlices = cqlSession.prepare("insert into files.file_upload_status (current_slice, total_slices, resource_type, resource_id, user_email, status_code) values (?, ?, ?, ?, ?, ?)");
        //addSlices = cqlSession.prepare("update file.file_upload_status set current_slice = current_slice + 1 where resourceId = ? and resourceType = ?");

    }

    public FileUploadStatus getUploadStatus(Long resourceId, String resourceType) {
        ResultSet execute = cqlSession.execute(getUploadStatus.bind( resourceId, resourceType));
        FileUploadStatus fileUploadStatus = FileServiceMapper.parseUploadStatus(execute);
        if (null == fileUploadStatus) {
            fileUploadStatus = new FileUploadStatus();
            fileUploadStatus.setResourceType(resourceType);
            fileUploadStatus.setStatusCode(0);
            return fileUploadStatus;
        }
        return fileUploadStatus;
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

    private boolean checkUploadFile(String resourceType, Long resourceId, Integer currentSlice, Integer totalSlice, MultipartFile file, String md5) throws IOException, NoSuchAlgorithmException {
        FileUploadStatus uploadStatus = getUploadStatus(resourceId, resourceType);
        if (null == uploadStatus && currentSlice != 0) {
            return false;
        }
        if (null != uploadStatus && currentSlice != uploadStatus.getCurrentSlice() + 1) {
            return false;
        }
        if (!checkSum(file, md5)) {
            return false;
        }
        return true;
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
        return abstraction.equals(md5);
    }

    public FileUploadStatus uploadFile(MultipartFile file, String type, Integer currentSlice, Integer totalSlice,
                                       Long resourceId, String userEmail, String checkSum,String totalCheckSum) throws IOException, NoSuchAlgorithmException {
        if (file.isEmpty()) {
            System.out.println("file is empty!");
            return null;
        }
        // 检查该分片是否正常
        boolean isValid = checkUploadFile(type, resourceId, currentSlice, totalSlice, file, checkSum);
        if (!isValid) {
            return null;
        }

        File fileToWrite = new File("./tmp/" +type + "_" + resourceId + "_" + currentSlice + "_" + totalSlice + "_" + checkSum);
        file.transferTo(fileToWrite);

        //额外处理
        if (currentSlice == 0) {
            //第一个分段
            cqlSession.execute(addSlices.bind(0, totalSlice, type, resourceId, userEmail, 1));
        }
        else if (currentSlice.equals(totalSlice)) {
            //最后一个分段
            cqlSession.execute(addSlices.bind(totalSlice, totalSlice, type, resourceId, userEmail, 2));
            executor.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    String path = "./tmp/type_" + resourceId;
                    try (FileOutputStream fis = new FileOutputStream(path)){
                        for (int i = 0; i < totalSlice; i ++) {
                            try (FileInputStream subFiles = new FileInputStream("./tmp/" + type + "_" + resourceId + "_" + i + "_" + totalSlice)) {
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = subFiles.read(buffer)) != -1) {
                                    fis.write(buffer, 0, bytesRead);
                                }
                            }
                            catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            boolean b = checkSum(path, totalCheckSum);
                            if (!b) {
                                throw new Exception("检验失败");
                            }
                            cqlSession.execute(addSlices.bind(totalSlice, totalSlice, type, resourceId, userEmail, 3));
                        }
                    } catch (Exception e) {
                        throw new Exception(e);
                    }
                    return false;

                }

            });
        } else {
            // 中间分段
            cqlSession.execute(addSlices.bind(currentSlice , totalSlice, type, resourceId, userEmail, 1));
        }
        return new FileUploadStatus();
    }
}
