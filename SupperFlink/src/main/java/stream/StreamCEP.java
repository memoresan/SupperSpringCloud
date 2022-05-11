package stream;

import core.FlinkExecutionEnvironment;
import entity.Senior;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import stream.window.CustomWatermarkStrategy;

import java.time.Duration;

public class StreamCEP {
    static StreamExecutionEnvironment env = FlinkExecutionEnvironment.EXE_ENV;
    public static void main(String[] agrs) throws Exception {
       // StreamExecutionEnvironment env = FlinkExecutionEnvironment.EXE_ENV;
        // FlinkExecutionEnvironment.setCheckPoint(env);
        //source 源
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        DataStreamSource<String> inputDataStream = env.socketTextStream("192.168.16.176", 3333);
        SingleOutputStreamOperator<Senior> seniorOperator = inputDataStream.flatMap(new FlatMapFunction<String, Senior>() {

            @Override
            public void flatMap(String value, Collector<Senior> out) throws Exception {
                String[] split = value.split(",");
                Senior senior = new Senior();
                senior.setName(split[0]);
                senior.setAge(Integer.valueOf(split[1]));
                senior.setTimestamp(Long.parseLong(split[2]));
                out.collect(senior);
            }
        });

      //  seniorOperator.print();
        SingleOutputStreamOperator<Senior> seniorSingleOutputStreamOperator = seniorOperator.assignTimestampsAndWatermarks(
               new CustomWatermarkStrategy<Senior>(1000).withIdleness(Duration.ofMillis(1))
        );
       // seniorSingleOutputStreamOperator.print();
        cepDemo(seniorSingleOutputStreamOperator);
    }

    public static void cepDemo( SingleOutputStreamOperator<Senior>  keyedStream) throws Exception {
        Pattern<Senior,Senior> pattern = Pattern.<Senior>begin("begin").where(new SimpleCondition<Senior>() {
            @Override
            public boolean filter(Senior value) throws Exception {
                System.out.println(value.getName().equals("z"));
                return value.getName().equals("z");
            }
        }).times(2).consecutive().within(Time.seconds(3));
        //.within(Time.seconds(5));
        PatternStream<Senior> patternStream = CEP.pattern(keyedStream, pattern);
        SingleOutputStreamOperator<String> select = patternStream.select((PatternSelectFunction<Senior, String>)
                pattern1 -> pattern1.get("begin").toString());
        select.print();
        env.execute();
    }

}
