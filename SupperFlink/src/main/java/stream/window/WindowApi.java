package stream.window;

import core.FlinkExecutionEnvironment;
import entity.Senior;
import org.apache.commons.collections.IteratorUtils;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.RichAggregateFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AggregateApplyWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.*;
import org.apache.flink.streaming.api.windowing.evictors.Evictor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;

import java.time.Duration;
import java.util.Iterator;

public class WindowApi {
    public static void main(String[] agrs) throws Exception {
        StreamExecutionEnvironment env = FlinkExecutionEnvironment.getEnvironment(true);
        //source 源
        DataStreamSource<String> inputDataStream = env.socketTextStream("192.168.16.176", 3333);
        SingleOutputStreamOperator<Senior> operator = inputDataStream.flatMap((value, out)->{
            String[] split = value.split(",");
            Senior senior =new Senior();
            senior.setName(split[0]);
            senior.setAge(Integer.parseInt(split[1]));
            senior.setTimestamp(Long.parseLong(split[2]));
            out.collect(senior);

        });
        operator.returns(TypeInformation.of(Senior.class));

        //window 必须是keyby
        KeyedStream<Senior, String> seniorKeyedStream = operator.keyBy(new KeySelector<Senior, String>() {
            @Override
            public String getKey(Senior value) throws Exception {
                return value.getName();
            }
        });

        tumbingWindow(seniorKeyedStream);
        env.execute();

    }

    //区别1 就是我们不需要设置时间语义  keyedStream.timeWindow() 而是采用window直接进入对应的时间
    public static void tumbingWindow(KeyedStream<Senior, String> keyedStream) {

        SingleOutputStreamOperator<Senior> seniorStreamOperator = keyedStream.
                assignTimestampsAndWatermarks(new CustomWatermarkStrategy<Senior>(3000).withIdleness(Duration.ofSeconds(2)));
        //seniorStreamOperator.print();
        //不区分key
        // seniorStreamOperator.keyBy("name").window(CustomTimeWindows.of(false,Time.seconds(5)));
        seniorStreamOperator.keyBy(new KeySelector<Senior, Object>() {
            @Override
            public Object getKey(Senior value) throws Exception {
                return value.getName();
            }
        }).window(SlidingEventTimeWindows.of(Time.seconds(5), Time.seconds(2))).evictor(new Evictor<Senior, TimeWindow>() {
            @Override
            public void evictBefore(Iterable<TimestampedValue<Senior>> elements, int size, TimeWindow window, EvictorContext evictorContext) {
                Iterator<TimestampedValue<Senior>> iterator = elements.iterator();
                while(iterator.hasNext()){
                    Senior value = iterator.next().getValue();
                    if(value.getAge() > 1){
                        iterator.remove();
                    }
                }
            }

            @Override
            public void evictAfter(Iterable<TimestampedValue<Senior>> elements, int size, TimeWindow window, EvictorContext evictorContext) {

            }
        }).allowedLateness(Time.seconds(1))
                .aggregate(new AggregateFunction<Senior, Integer, Integer>() {
            //产生初始值
            @Override
            public Integer createAccumulator() {
                return 0;
            }

            @Override
            public Integer add(Senior value, Integer accumulator) {
                return accumulator.intValue() + value.getAge();
            }

            @Override
            public Integer getResult(Integer accumulator) {
                return accumulator.intValue();
            }

            @Override
            public Integer merge(Integer a, Integer b) {
                return a + b;
            }
        }).print();
        //SlidingProcessingTimeWindows

        seniorStreamOperator.keyBy("name").window(EventTimeSessionWindows.withGap(Time.seconds(2))).sum("age").print();
    }



}
