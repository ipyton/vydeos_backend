package com.chen.blogbackend.util;

import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {
    public static ByteArrayInputStream processImage(InputStream inputStream, int targetMinimumByteSize, int targetMaximumByteSize, int widthAndHeight) throws IOException {
        double quality = 0.85;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int i = 0; i < 3; i++) {
            byteArrayOutputStream.reset();
            Thumbnails.of(inputStream)
                    .size(widthAndHeight, widthAndHeight)
                    .outputFormat("jpg")
                    .outputQuality(quality)
                    .toOutputStream(byteArrayOutputStream);

            int currentSize = byteArrayOutputStream.size();
            System.out.println("Current compressed size: " + currentSize / 1024 + " KB, quality: " + quality);

            if (currentSize >= targetMinimumByteSize && currentSize <= targetMaximumByteSize) {
                break;
            } else if (currentSize > targetMaximumByteSize) {
                quality -= 0.05;
                if (quality < 0.5) quality = 0.5;
            } else {
                quality += 0.05;
                if (quality > 0.95) quality = 0.95;
            }
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
