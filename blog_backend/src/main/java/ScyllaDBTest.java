import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import org.apache.catalina.Cluster;

import java.net.InetSocketAddress;

public class ScyllaDBTest {
    public static String[] contact_points = {"101.132.222.131"};
    public static int port = 9042;


    public static void connect() {
        CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("101.132.222.131", 9042))
                .withLocalDatacenter("datacenter1")
                .build();
        session.close();
    }

    public static void query() {
    }

    public static void loadData() {

    }

    public static void close() {
    }

    public static void main(String[] args) {
        connect();


    }




}
