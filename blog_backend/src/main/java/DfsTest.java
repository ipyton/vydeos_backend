import org.csource.common.MyException;
import org.csource.fastdfs.*;

import java.io.IOException;

@Deprecated
public class DfsTest {
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
        } catch(IOException e) {
            e.printStackTrace();
        } catch(MyException e) {
            e.printStackTrace();
        }


    }
}
