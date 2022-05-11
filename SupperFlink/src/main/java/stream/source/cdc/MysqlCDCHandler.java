package stream.source.cdc;

import com.ververica.cdc.connectors.mysql.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.DebeziumSourceFunction;
import core.FlinkExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStreamSource;

import java.util.Properties;

public class MysqlCDCHandler {
    public static void main(String[] agrs) throws Exception {

        Properties properties = new Properties();
        properties.setProperty("scan.incremental.snapshot.enabled","true");
        properties.setProperty("scan.incremental.snapshot.chunk.size","4096");
        DebeziumSourceFunction<String> mysqlSource = MySqlSource.<String>builder().hostname("192.168.16.176")
                .port(3307).username("root").password("zty123456").databaseList("flink_cdc").tableList("flink_cdc.flink_cdc_test")
                .startupOptions(StartupOptions.initial())
                .debeziumProperties(properties)
                .deserializer(new myDeserializationSchema())
                .build();
        DataStreamSource<String> mysqlDS = FlinkExecutionEnvironment.EXE_ENV.addSource(mysqlSource);
        mysqlDS.print();
        FlinkExecutionEnvironment.EXE_ENV.execute();
    }
}
