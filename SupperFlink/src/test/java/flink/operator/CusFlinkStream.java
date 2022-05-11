package flink.operator;

import core.FlinkExecutionEnvironment;
import flink.operator.cus.function.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class CusFlinkStream {
    public static void main(String[] agrs) throws Exception {
        StreamExecutionEnvironment env = FlinkExecutionEnvironment.getEnvironment(true);
        //source 源
        DataStreamSource<String> inputDataStream = env.socketTextStream("192.168.16.176", 1111);
        CusDataStream<String> cusFlinkStream = new CusDataStream<String>(inputDataStream.getExecutionEnvironment(),inputDataStream.getTransformation());
        cusFlinkStream.cusMap(new MapFunction<String, String>() {
            @Override
            public String map(String value) throws Exception {
                System.out.println(value);
                return value;
            }
        }).print();
        env.execute();

    }



}
