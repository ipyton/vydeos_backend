package com.chen.blogbackend.util;

import com.chen.blogbackend.entities.UnfinishedUpload;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class VideoUtil {


    public static void concat(UnfinishedUpload upload) throws Exception {
        File file = new File(upload.getOwnerName());
        FileWriter writer = new FileWriter(file);
        char[] cs = new char[1024];
        for (int i = 0; i < upload.getTotal(); i ++ ) {
            File fileSlice = new File(upload.getFileHash() + "_" + i);
            FileReader reader = new FileReader(fileSlice);
            if (fileSlice.length() == 0) {
                throw new Exception("file length is 0");
            }
            int read;
            while((read = reader.read(cs)) > 0 ) {
                writer.write(cs, 0, read);
            }
            reader.close();
        }
        writer.close();
    }


    public static void convertToHLS(UnfinishedUpload upload) {

    }



    public static void main(String[] args) {

    }
}
