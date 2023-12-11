package com.chen.blogbackend.util;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Deprecated
@Service
public class FastDFSClientUtil {
    public static void main(String[] args) {
        try{
            //加载配置文件
            ClientGlobal.init("src/main/resources/client.conf");
            //tracker客户端对象
            TrackerClient trackerClient=new TrackerClient();
            //tracker服务端对象
            TrackerServer trackerServer=trackerClient.getConnection();
            //storage服务端对象
            StorageServer storageServer=trackerClient.getStoreStorage(trackerServer);
            //最终需要的对象，即storage客户端对象
            StorageClient storageClient=new StorageClient(trackerServer,storageServer);
            System.out.println("storageClient:"+storageClient);
            NameValuePair[] meta_list = new NameValuePair[1];
            meta_list[0] = new NameValuePair("author", "ppp");
            String[] results = storageClient.upload_file("client".getBytes(StandardCharsets.UTF_8), "conf", meta_list);
            for (String s: results
                 ) {
                System.out.println(s);
            }

        }catch(IOException e){
            e.printStackTrace();
        }catch(MyException e){
            e.printStackTrace();
        }


    }
}