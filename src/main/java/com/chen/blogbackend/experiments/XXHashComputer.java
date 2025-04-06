package com.chen.blogbackend.experiments;

import net.jpountz.xxhash.StreamingXXHash64;
import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class XXHashComputer {

    /**
     * Computes xxHash for a file using chunked processing
     *
     * @param filePath path to the file
     * @param chunkSize size of chunks to process (default: 32MB)
     * @return xxHash value as a hex string
     * @throws IOException if file reading fails
     */

    public static String computeXxHash(String filePath, int chunkSize) throws IOException {
        // Default chunk size if not specified (32MB)
        if (chunkSize <= 0) {
            chunkSize = 32 * 1024 * 1024;
        }

        // Get xxHash instance
        XXHashFactory factory = XXHashFactory.fastestInstance();
        StreamingXXHash64 hasher = factory.newStreamingHash64(990816L); // 使用流式哈希

        File file = new File(filePath);
        long totalBytes = 0;

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[chunkSize];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                // 更新哈希计算，只处理实际读取的字节数
                hasher.update(buffer, 0, bytesRead);

                totalBytes += bytesRead;
                System.out.printf("Processed: %.2f MB%n", totalBytes / (1024.0 * 1024.0));
            }

            // 获取最终哈希值
            long hash = hasher.getValue();
            return Long.toHexString(hash);
        }
    }


    /**
     * Overloaded method with default chunk size
     */
    public static String computeHash(String filePath) throws IOException {
        return computeXxHash(filePath, 32 * 1024 * 1024);
    }

    public static String computeHash(InputStream inputStream, int chuckSize) throws IOException {
            int chunkSize = 32 * 1024 * 1024; // 默认 32MB 块大小
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            long totalBytes = 0;

            // 获取 xxHash 工厂和哈希器
            XXHashFactory factory = XXHashFactory.fastestInstance();
            StreamingXXHash64 hasher = factory.newStreamingHash64(990816L);

            try {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    hasher.update(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    System.out.printf("Processed: %.2f MB%n", totalBytes / (1024.0 * 1024.0));
                }
                long hash = hasher.getValue();
                return Long.toHexString(hash);
            } finally {
                inputStream.close(); // 确保关闭流
            }
        }



    /**
     * Example usage
     */
    public static void main(String[] args) {
        try {
            long start = System.currentTimeMillis();
            String filePath = "/Volumes/kinston/testing.zip.zip";
            String hash = computeXxHash(filePath,-1);
            System.out.println("xxHash: " + hash  );
            System.out.println(System.currentTimeMillis() - start);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}