package flinksql.cdc;

import core.FlinkExecutionEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.poi.util.SystemOutLogger;

public class MysqlCDCHandler {
    public static void main(String[] agrs) throws Exception {
        StreamTableEnvironment tableEnv = FlinkExecutionEnvironment.TABLE_STREAM_ENV;
            //2.创建 Flink-MySQL-CDC 的 Source
            tableEnv.executeSql("CREATE TABLE user_info (" +
                    " name STRING primary key" +
                    ") WITH (" +
                    " 'connector' = 'mysql-cdc'," +
                    " 'hostname' = '192.168.16.176'," +
                    " 'port' = '3307'," +
                    //是否一定要有组件
                    " 'scan.incremental.snapshot.enabled' = 'true'," +
                    " 'username' = 'root'," +
                    " 'password' = '1'," +
                    " 'database-name' = 'flink_cdc'," +
                    " 'table-name' = 'flink_cdc_test'" +
                    "'scan.startup.mode'='initial'" +
                    ")");
            tableEnv.executeSql("select * from user_info").print();
            FlinkExecutionEnvironment.EXE_ENV.execute();
    }

}
