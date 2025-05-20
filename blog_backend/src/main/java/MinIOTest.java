import io.minio.*;
import io.minio.messages.Bucket;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class MinIOTest {

    public static void main(String[] args) {
        try {
            // 创建客户端
            MinioClient minioClient =
                    MinioClient.builder()
                            // api地址
                            .endpoint("http://180.164.75.24:8090")
//                            .endpoint("http://127.0.0.1:9000")
                            // 前面设置的账号密码
                            .credentials("minioadmin", "minioadmin")
                            .build();

            System.out.println(minioClient);
            // 检查桶是否存在
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket("test").build());
            if (!found) {
                // 创建桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("test").build());
            }

            //列出所有桶名
            List<Bucket> buckets = minioClient.listBuckets();
            for (Bucket i : buckets){
                System.out.println(i.name());
            }

            //删除某一个桶
//            minioClient.removeBucket(
//                    RemoveBucketArgs.builder()
//                            .bucket("桶名称")
//                            .build());



            System.out.println("开始你的操作");

            File file = new File("D:\\client.conf");

            String fileName = file.getName();
            String realFileName = fileName.substring(fileName.lastIndexOf("\\")+1, fileName.lastIndexOf("."));
            String fileType = fileName.substring(fileName.lastIndexOf(".")+1);

            //通过路径上传一个文件
            ObjectWriteResponse testDir = minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("test")
                            .object("user_Path1")//文件名字
                            .filename("D:\\client.conf")//文件存储的路径
                            .contentType(fileType)
                            .build());

            //通过文件格式上传一个文件
            InputStream fileInputStream = new FileInputStream(file);
            long size = file.length();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("test")
                            .object("user_File1")
                            .stream(fileInputStream, size, -1)
                            .contentType(fileType)
                            .build());

            //文件下载，都是这种下载到指定路径
            minioClient.downloadObject(DownloadObjectArgs.builder()
                    .bucket("test")
                    .object("user_File1")
                    .filename("D:\\user_test.conf")
                    .build());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
