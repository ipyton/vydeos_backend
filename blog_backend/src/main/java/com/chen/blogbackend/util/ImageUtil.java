package com.chen.blogbackend.util;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;

import java.io.*;

public class ImageUtil {

    private static final Tika tika = new Tika();

    public static ByteArrayInputStream processImage(InputStream inputStream, int targetMinimumByteSize, int targetMaximumByteSize, int widthAndHeight) throws IOException {
        // 先将 InputStream 读取为字节数组，避免重复读取问题
        byte[] imageBytes = inputStream.readAllBytes();
        int originalSize = imageBytes.length;

        System.out.println("Original image size: " + originalSize / 1024 + " KB");

        // 如果原始大小已经在目标范围内，直接返回
        if (originalSize >= targetMinimumByteSize && originalSize <= targetMaximumByteSize) {
            System.out.println("Original size is within target range, no compression needed.");
            return new ByteArrayInputStream(imageBytes);
        }

        double quality = 0.85;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        for (int i = 0; i < 3; i++) {
            byteArrayOutputStream.reset();

            // 每次循环都使用新的 ByteArrayInputStream
            ByteArrayInputStream byteInput = new ByteArrayInputStream(imageBytes);

            Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(byteInput)
                    .outputFormat("jpg")
                    .outputQuality(quality);

            // 如果 widthAndHeight 不为 0，才设置尺寸，否则保留原比例
            if (widthAndHeight > 0) {
                builder.size(widthAndHeight, widthAndHeight);
            }

            builder.toOutputStream(byteArrayOutputStream);

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
