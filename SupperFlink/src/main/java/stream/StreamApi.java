package stream;
import core.FlinkExecutionEnvironment;
import entity.Senior;
import org.apache.flink.api.common.functions.*;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.typeutils.MapTypeInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.execution.librarycache.FlinkUserCodeClassLoaders;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.runtime.state.PartitionableListState;
import org.apache.flink.streaming.api.TimeDomain;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.checkpoint.ListCheckpointed;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StreamApi {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = FlinkExecutionEnvironment.EXE_ENV;
       // FlinkExecutionEnvironment.setCheckPoint(env);
        //source 源
        DataStreamSource<String> inputDataStream = env.socketTextStream("192.168.16.176", 3333);
        //转换算子
        SingleOutputStreamOperator<Tuple2<String, Integer>> operator = inputDataStream.flatMap((value,out)->{
                String[] split = value.split(",");
                for (String str : split) {
                    out.collect(Tuple2.of(str, 1));
                }
            });
        operator.returns(TypeInformation.of(new TypeHint<Tuple2<String, Integer>>() {}));

        KeyedStream<Tuple2<String, Integer>,String> keyedStream = operator.keyBy(new KeySelector<Tuple2<String, Integer>, String>() {
            public String getKey(Tuple2<String, Integer> value) throws Exception {
                return value.f0;
            }
        });

        SingleOutputStreamOperator<Tuple2<String, Integer>> reduceOpertor = keyedStream.reduce(new ReduceFunction<Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> reduce(Tuple2<String, Integer> value1, Tuple2<String, Integer> value2) throws Exception {
                return Tuple2.of(value1.f0, value1.f1 + value2.f1);
            }
        });
        //reduceOpertor.print();
        //reduceOpertor.print("----reduceOperator");
        //splitMethod(reduceOpertor);
       // operatorState(keyedStream);
       // brocastStream(reduceOpertor);
       // StreamOnTime(reduceOpertor);
        //streamPartitiioner(reduceOpertor);
        //operatorState(operator);
       // KeyedValueStreamState(keyedStream);
        env.execute();

    }

    private static void streamPartitiioner(SingleOutputStreamOperator<Tuple2<String, Integer>> reduceOpertor) {
        reduceOpertor.partitionCustom(new Partitioner<String>() {
            @Override
            public int partition(String key, int numPartitions) {
                return key.hashCode() % numPartitions;
            }
        }, new KeySelector<Tuple2<String, Integer>, String>() {
            @Override
            public String getKey(Tuple2<String, Integer> value) throws Exception {
                return value.f0;
            }
        });



    }

    /**
     * 拆分 合并
     * @param reduceOpertor
     */
    public static void splitMethod(DataStream<Tuple2<String, Integer>> reduceOpertor){
        //拆分
        //表示奇数流
        OutputTag<Tuple2<String, Integer>> oddTag = new OutputTag<Tuple2<String, Integer>>("odd"){};
        OutputTag<Tuple2<String, Integer>> evenTag = new OutputTag<Tuple2<String, Integer>>("even"){};
        //入参数数据 出参是类型 SingleOutputStreamOperator类里面独有 getSideOutput方法
        SingleOutputStreamOperator<Tuple2<String, Integer>> process = reduceOpertor.process(new ProcessFunction<Tuple2<String, Integer>, Tuple2<String, Integer>>() {
            //out 是process 的输出流可以不输出可以输出多个
            @Override
            public void processElement(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
                if (value.f1 % 2 == 0) {
                    ctx.output(evenTag, value);
                } else {
                    ctx.output(oddTag, value);
                }
            }
        });
        //如果类型相同直接合并 拆分的时候会分成两个分区
        DataStream<Tuple2<String, Integer>> odd = process.getSideOutput(oddTag);
        DataStream<Tuple2<String, Integer>> even = process.getSideOutput(evenTag);
        odd.union(even).print("union----");


        //这个是select合并
        SingleOutputStreamOperator<Tuple3<String, String, Integer>> newOdd = odd.map(x -> {
            return new Tuple3<String, String, Integer>("奇数", x.f0, x.f1);
        }).returns(Types.TUPLE(Types.STRING, Types.STRING, Types.INT));
        newOdd.connect(even).map(new CoMapFunction<Tuple3<String, String, Integer>, Tuple2<String, Integer>, Object>() {
            @Override
            public Object map1(Tuple3<String, String, Integer> value) throws Exception {
                return value;
            }

            @Override
            public Object map2(Tuple2<String, Integer> value) throws Exception {
                return value;
            }
        }).print();

    }

    /**
     * 利用mapState完成聚合任务
     * @param reduceOpertor
     */
    public static void KeyedStreamState(DataStream<Tuple2<String, Integer>> reduceOpertor){
        reduceOpertor.map(new RichMapFunction<Tuple2<String, Integer>, Tuple2<String, Integer>>() {
            private ValueState<Integer> valueState;
            private MapState<String,Integer> mapState;
            //入参和出参必须是同一个类型
            private ReducingState<Integer> reducingState;
            //入参和出参是不同类型
            private AggregatingState<Tuple2<String,Integer>,Integer> aggregatingState;
            @Override
            public Tuple2<String, Integer> map(Tuple2<String, Integer> value) throws Exception {
                reducingState.add(value.f1);
                System.out.println("reduce--------"+Thread.currentThread().getName()+"----"+reducingState.get());
                aggregatingState.add(value);
                System.out.println("aggregate--------"+Thread.currentThread().getName()+"----"+aggregatingState.get());
                //valueState.update(valueState.value() + 1);
                if(mapState.contains(value.f0)){
                    mapState.put(value.f0, mapState.get(value.f0)+ value.f1);
                }else {
                    mapState.put(value.f0, value.f1);
                }
                return Tuple2.of(value.f0, mapState.get(value.f0));

            }

            @Override
            public void open(Configuration parameters) throws Exception {
                ValueStateDescriptor<Integer> valueStateDescriptor =
                        new ValueStateDescriptor<Integer>("odd",TypeInformation.of(Integer.class));
                //如果是tuple的话 可以使用TypeInfomation.of(new TypeHint<Tuple<String,String>>(){}) 来代替 Types.Tuple()
                MapStateDescriptor mapStateDescriptor = new MapStateDescriptor("count",TypeInformation.of(String.class),TypeInformation.of(Integer.class));



                ReducingStateDescriptor<Integer> reducingStateDescriptor = new ReducingStateDescriptor(
                        "reduce", new ReduceFunction<Integer>() {
                    @Override
                    public Integer reduce(Integer value1, Integer value2) throws Exception {
                        return value1 + value2;
                    }
                },TypeInformation.of(Integer.class));



                // 输入类型，中间值类型，输出类型
                AggregatingStateDescriptor<Tuple2<String,Integer>,Integer,Integer> aggregatingStateDescriptor = new AggregatingStateDescriptor("aggre", new AggregateFunction<Tuple2<String,Integer>,Integer,Integer>() {
                    //初始化返回类型acc 中间类型
                    @Override
                    public Integer createAccumulator() {
                        return 0;
                    }

                    @Override
                    public Integer add(Tuple2<String, Integer> value, Integer accumulator) {
                        return value.f1+accumulator;
                    }

                    @Override
                    public Integer getResult(Integer accumulator) {
                        return accumulator;
                    }

                    @Override
                    public Integer merge(Integer a, Integer b) {
                        return a+b;
                    }
                },TypeInformation.of(Integer.class).createSerializer(getRuntimeContext().getExecutionConfig()));


                valueState = getRuntimeContext().getState(valueStateDescriptor);
                mapState = getRuntimeContext().getMapState(mapStateDescriptor);
                reducingState = getRuntimeContext().getReducingState(reducingStateDescriptor);
                aggregatingState = getRuntimeContext().getAggregatingState(aggregatingStateDescriptor);
            }
        }).print();
    }

    public static void operatorState( SingleOutputStreamOperator<Tuple2<String, Integer>> reducerOperator ){
        OutputTag<String> warning = new OutputTag<String>("报警", TypeInformation.of(String.class));
        SingleOutputStreamOperator<Tuple2<String, Integer>> processValue = reducerOperator.process(new CusProcessFunction(warning)).setParallelism(1);
        processValue.getSideOutput(warning).print();
        processValue.print();
    }

    public static void KeyedValueStreamState(DataStream<Tuple2<String, Integer>> reduceOpertor){
        reduceOpertor.map(new RichMapFunction<Tuple2<String, Integer>, Tuple2<String, Integer>>() {
            private ValueState<Integer> valueState;

            @Override
            public Tuple2<String, Integer> map(Tuple2<String, Integer> value) throws Exception {
                valueState.update(value.f1);
                return value;
            }

            @Override
            public void open(Configuration parameters) throws Exception {
                ValueStateDescriptor<Integer> valueStateDescriptor =
                        new ValueStateDescriptor<Integer>("odd", TypeInformation.of(Integer.class));
                valueState =  getRuntimeContext().getState(valueStateDescriptor);
            }

        }).print();
    }






    public static void brocastStream(DataStream<Tuple2<String, Integer>> reduceOpertor){
        StreamExecutionEnvironment env = FlinkExecutionEnvironment.getEnvironment(true);

        //创建brocast流
        DataStreamSource<String> inputDataStream = env.socketTextStream("192.168.16.176", 2222);
        //在单值的时候是可以使用mapstate描述器的但是不能使用mapState这种状态
        MapStateDescriptor<String,Integer> mapStateDescriptor = new MapStateDescriptor<String,Integer>("map",Types.STRING,Types.INT);
        BroadcastStream<String> broadcast = inputDataStream.broadcast(mapStateDescriptor);
        reduceOpertor.connect(broadcast).process(new BroadcastProcessFunction<Tuple2<String, Integer>, String,Tuple2<String, Integer>>() {
            private final MapStateDescriptor<String, Integer> broadCastConfigDescriptor = new MapStateDescriptor<>("map", Types.STRING,Types.INT);
            @Override
            public void processElement(Tuple2<String, Integer> value, ReadOnlyContext ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
                ReadOnlyBroadcastState<String, Integer> broadcastState = ctx.getBroadcastState(broadCastConfigDescriptor);
                int response = 0;
                if(broadcastState.contains(value.f0)){
                    response = broadcastState.get(value.f0);

                }
                out.collect(Tuple2.of(value.f0,value.f1+response));
            }

            @Override
            public void processBroadcastElement(String value, Context ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
                BroadcastState<String, Integer> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
                if(broadcastState.contains(value)){
                    broadcastState.put(value,broadcastState.get(value) + 1);
                }else{
                    broadcastState.put(value,1);
                }
            }

            @Override
            public void open(Configuration parameters) throws Exception {
                //这个东西只能在离线里面使用
               /* List<String> map = getRuntimeContext().getBroadcastVariable("map");
                map.add("1");*/

               /* getRuntimeContext().getBroadcastVariableWithInitializer("map", new BroadcastVariableInitializer<String, String>() {
                    @Override
                    public String initializeBroadcastVariable(Iterable<String> data) {
                        return null;
                    }
                });*/



            }
        }).print();


    }


    public static void StreamOnTime(DataStream<Tuple2<String, Integer>> reduceOpertor){
        reduceOpertor.keyBy(0).process(new KeyedProcessFunction<Tuple, Tuple2<String, Integer>, Tuple2<String, Integer>>() {
            @Override
            public void processElement(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
                if(value.f0.equals("1")){
                    //Setting timers is only supported on a keyed streams.
                    ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime()+5000);
                    ctx.timerService().deleteProcessingTimeTimer(5000);
                    // ctx.timerService().registerEventTimeTimer();

                }
            }

            @Override
            public void onTimer(long timestamp, OnTimerContext ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
                super.onTimer(timestamp, ctx, out);
                if(ctx.timeDomain().equals(TimeDomain.PROCESSING_TIME)){
                    out.collect(Tuple2.of("触发了",(int)timestamp));
                }
            }
        }).print();
    }




}



class CusProcessFunction extends ProcessFunction<Tuple2<String, Integer>, Tuple2<String, Integer>> implements CheckpointedFunction {
    private OutputTag<String> outputTag;
    //托管状态
    ListState<Long> listState;
    public CusProcessFunction(OutputTag<String> outputTag) {
        this.outputTag = outputTag;
    }
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }


    @Override
    public void processElement(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
        long state =listState.get().iterator().next();
        //int state = listState.get();
        if (state > 10) {
            ctx.output(outputTag, "---已经到达上线");
        }else{
            listState.update(Collections.singletonList(state+1));
        }

        System.out.println(state);
        out.collect(value);
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        //System.out.println(Thread.currentThread().getName());
        //listState.update(Collections.singletonList(list.get(0)));
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        ListStateDescriptor listStateDescriptor=new ListStateDescriptor("checkPointedList",
                TypeInformation.of(new TypeHint<Long>() {}));
        listState=context.getOperatorStateStore().getListState(listStateDescriptor);
        //如果没有恢复就是初始化
        if(!context.isRestored()){
            listState.add(0L);
        }
    }
}
