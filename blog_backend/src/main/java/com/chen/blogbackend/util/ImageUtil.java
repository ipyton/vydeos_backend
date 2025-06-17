package com.chen.blogbackend.util;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;

import java.io.*;

public class ImageUtil {

    private static final Tika tika = new Tika();

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

    public static String getType(InputStream inputStream) throws IOException {
        // Wrap InputStream with BufferedInputStream to allow mark/reset
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        bufferedInputStream.mark(10 * 1024); // mark position up to 10MB
        String mimeType = tika.detect(bufferedInputStream);
        bufferedInputStream.reset(); // reset to marked position for reuse
        return mimeType;
    }
}
