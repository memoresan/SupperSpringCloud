package util.hbase;

/*import com.github.CCweixiao.HBaseAdminTemplate;
import com.github.CCweixiao.HBaseSqlTemplate;
import com.github.CCweixiao.HBaseTemplate;
import com.github.CCweixiao.thrift.HBaseThriftService;
import com.github.CCweixiao.thrift.HBaseThriftServiceHolder;*/
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import util.yml.YmlUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HbaseUtils {
    private  static String zkPort;
    private static String ip;

    static {
        zkPort = YmlUtils.getProperties().getProperty("zookeeper.port");
        ip= YmlUtils.getProperties().getProperty("zookeeper.ip");
    }

    public static Connection getHbaseConnection(){
        Connection hbaseConn = null;
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", ip);
        conf.set("hbase.zookeeper.property.clientPort", zkPort);
        conf.set("hbase.client.ipc.pool.size","2");
        try {
           // ExecutorService executorService = Executors.newCachedThreadPool();
            hbaseConn = ConnectionFactory.createConnection(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hbaseConn;

    }

    public static void close(Connection connection, Table table) throws IOException {
        if (table != null) {
            table.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] agrs) throws IOException {
        getHbaseThriftConnection();
    }


    public static void getHbaseThriftConnection(){
        /*HBaseThriftService hBaseThriftService = HBaseThriftServiceHolder.getInstance("192.168.16.176", 9096);
        List<String> allTableNames = hBaseThriftService.getTableNames();
        String s = allTableNames.get(0);
        System.out.println(s);*/

    }
}
