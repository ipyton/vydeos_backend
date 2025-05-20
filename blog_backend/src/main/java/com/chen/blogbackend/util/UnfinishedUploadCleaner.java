package com.chen.blogbackend.util;

import com.chen.blogbackend.DAO.VideoUploadingDao;
import com.chen.blogbackend.entities.UnfinishedUpload;
import com.chen.blogbackend.mappers.VideoUploadingMapper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

public class UnfinishedUploadCleaner implements Job {

    static final long LIMIT = 4096;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        File diskPartition = new File("/");
        long usableSpace = diskPartition.getUsableSpace();
        usableSpace /= 1024 * 1024;
        if (usableSpace < LIMIT) {
            return;
        }
        String workingDir = (String) jobExecutionContext.get("workingDir");
        VideoUploadingDao dao = (VideoUploadingDao) jobExecutionContext.get("dao");
        List<UnfinishedUpload> all = dao.getAll();
        for (UnfinishedUpload upload: all
             ) {
            if (- upload.getStartTime() + System.currentTimeMillis() > 2 * 3600 * 1000) {
                String fileHash = workingDir + upload.getFileHash();
                for (int i = 0; i < upload.getCurrent(); i ++) {
                    String name = fileHash + "_" + i;
                    boolean delete = new File(name).delete();
                    if (!delete) {
                        try {
                            throw new Exception("delete error");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }


    }
}
