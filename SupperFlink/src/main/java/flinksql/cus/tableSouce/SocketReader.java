package flinksql.cus.tableSouce;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

/**
 * @Date 2021/8/30 10:52
 * @Version 1.0
 * @Description
 **/
public class SocketReader {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        String CreateTable = "CREATE TABLE UserScores (name STRING, score INT,order_time TIMESTAMP(3)," +
                " WATERMARK FOR order_time as order_time - INTERVAL '1' SECOND) \n" +
                " partitioned by (name) \n" +
                "WITH( \n" +
                "   'connector'='socket',\n" +
                "   'hostname'='192.168.16.176',\n" +
                "   'port'='1111',\n" +
                "   'byte-delimiter'='10',\n" +
                "   'format'='changelog-csv',\n" +
                "   'changelog-csv.column-delimiter' = ','\n" +
                ")";
        System.out.println(CreateTable);
        tableEnv.executeSql(CreateTable);

        String view = "SELECT name, SUM(score) FROM UserScores GROUP BY name";
        tableEnv.executeSql(view).print();
//        DataStream stream = tableEnv.toRetractStream(table, Row.class);
//        stream.print();
    }
}
