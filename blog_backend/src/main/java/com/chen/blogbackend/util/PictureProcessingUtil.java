package com.chen.blogbackend.util;

import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;

public class PictureProcessingUtil {

    public static String generateThumbnail(String path) throws IOException {
        String newPath = path + "_thumbnail.jpg";
        Thumbnails.of(new File(path))
                .size(160, 160)
                .toFile(new File(path + "_thumbnail.jpg"));
        return newPath;
    }


}
