package flinksql;

import core.FlinkExecutionEnvironment;
import entity.FieldInfo;
import entity.Senior;
import flinksql.cus.watermarks.CusTimestampAssigner;
import flinksql.cus.watermarks.CusWatermarkStrategy;
import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssignerSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.typeinfo.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.operators.StreamFlatMap;
import org.apache.flink.streaming.api.operators.StreamMap;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.DataTypeFactory;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.descriptors.Csv;
import org.apache.flink.table.descriptors.FileSystem;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.table.sources.DefinedProctimeAttribute;
import org.apache.flink.table.sources.DefinedRowtimeAttributes;
import org.apache.flink.table.sources.RowtimeAttributeDescriptor;
import org.apache.flink.table.sources.StreamTableSource;
import org.apache.flink.table.sources.tsextractors.ExistingField;
import org.apache.flink.table.sources.wmstrategies.AscendingTimestamps;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.utils.LegacyTypeInfoDataTypeConverter;
import org.apache.flink.table.types.utils.TypeConversions;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import stream.window.CustomWatermarkStrategy;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static core.FlinkExecutionEnvironment.TABLE_STREAM_ENV;
import static org.apache.flink.table.api.Expressions.*;

public class FlinkSqlApi {
    public static void main(String[] args) throws Exception {
        FlinkExecutionEnvironment.init();
        //tableApi(FlinkExecutionEnvironment.TABLE_STREAM_ENV);
        //tableTimeProcess(FlinkExecutionEnvironment.EXE_ENV);
        //getTableCatalog(FlinkExecutionEnvironment.EXE_ENV);
        //tableTimeRow(FlinkExecutionEnvironment.EXE_ENV);
        streamToTable(FlinkExecutionEnvironment.EXE_ENV);
        FlinkExecutionEnvironment.EXE_ENV.execute();
    }

    public static void tableApi(StreamTableEnvironment tableEnvironment) throws Exception {
        String filePath = FlinkSqlApi.class.getClassLoader().getResource("sensor.txt").getPath();
        String filePath1 = FlinkSqlApi.class.getClassLoader().getResource("").getPath();

        tableEnvironment.connect( new FileSystem().path(filePath))
                .withFormat( new Csv())
                .withSchema( new Schema()
                        .field("id", DataTypes.STRING())
                        .field("timestamp", DataTypes.BIGINT())
                        .field("temp", DataTypes.DOUBLE())
                )
                .createTemporaryTable("inputTable");

        Table inputTable = tableEnvironment.from("inputTable");
        //首先group by 要放到前面
        Table resultTable = inputTable.groupBy($("id"), $("temp")).select($("id"), $("temp").sum().as("rev"))
                .filter($("id").isEqual("sensor_6"));

        //这个方法会被executeSql替代调
        tableEnvironment.connect(new FileSystem().path(filePath1+"/sensor1.txt")).withFormat(new Csv()).
                withSchema(new Schema().field("id", DataTypes.STRING())
                        .field("temp", DataTypes.DOUBLE())).createTemporaryTable("outputTable");
        //由表名转成table
        //每一个分区会生成一个文件，将指定的表加入到对应的表中，
        //注意这个表必须是由sink注册的表 第二个参数表示是否覆盖
        //TableResult tableResult = resultTable.executeInsert("outputTable",true);

        //resultTable.execute().print();
        //可以直接对某一个table进行打印 但是会报错
        //executeSql执行一般为ddl语句会返回TableResult,调用print方法可以打印 如果想执行sql其他语句一般使用 sqlQuery
        Table t = tableEnvironment.sqlQuery("select id from inputTable");
        SingleOutputStreamOperator<Tuple2<Boolean, Row>> returns = tableEnvironment.toRetractStream(t, Row.class).filter(x -> {
            return x.f0;
        }).returns(Types.TUPLE(TypeInformation.of(Boolean.class),Types.ROW(TypeInformation.of(String.class))));
        //.returns(TypeInformation.of(new TypeHint<Tuple2<Boolean,Row>>(){}));

        returns.map(x->x.f1).print();

       /* TableResult tableResult = resultTable.executeInsert("outputTable");
        tableResult.print();*/
    }

    public static void streamToTable(StreamExecutionEnvironment env) throws Exception {
        // 从socket文本流读取数据
        DataStream<String> inputDataStream = env.socketTextStream("192.168.16.176", 2222);
        List<FieldInfo> list = new ArrayList<>();
        list.add(new FieldInfo("a","int"));
        list.add(new FieldInfo("b","string"));
        list.add(new FieldInfo("c","int"));
        //row 转换
    /*    TypeInformation<Row> rowTypeInfo = getRowTypeInfo(list);
        SingleOutputStreamOperator<Row> rowOperator = inputDataStream.transform("map", rowTypeInfo, new StreamFlatMap<String, Row>(new FlatMapFunction<String, Row>() {
            @Override
            public void flatMap(String value, Collector<Row> out) throws Exception {
                String[] split = value.split(",");
                out.collect(Row.of(split[0], split[1], split[2]));
            }
        }));
        Table table1 = FlinkExecutionEnvironment.TABLE_STREAM_ENV.fromDataStream(rowOperator,$("a"),$("b"),$("c"));*/

        //tuple也可以
        SingleOutputStreamOperator<Tuple3<String, String, String>> mapOperator = inputDataStream.map(new RichMapFunction<String, Tuple3<String, String, String>>() {
            @Override
            public Tuple3<String, String, String> map(String value) throws Exception {
                String[] split = value.split(",");
                return Tuple3.of(split[0], split[1], split[2]);
            }
        });
        SingleOutputStreamOperator<Senior> seniorOperator = mapOperator.map(new MapFunction<Tuple3<String, String, String>, Senior>() {
            @Override
            public Senior map(Tuple3<String, String, String> value) throws Exception {
                return new Senior(1, "2", 3);
            }
        });
        Table table1 = TABLE_STREAM_ENV.fromDataStream(seniorOperator,$("name").as("a"));
        Table table = TABLE_STREAM_ENV
                .fromDataStream(mapOperator,$("f0").as("d"),$("f1").as("c"),$("f2").as("e"));
        TABLE_STREAM_ENV.createTemporaryView("test",table);
        TABLE_STREAM_ENV.createTemporaryView("test1",table1);
        Table result1 = TABLE_STREAM_ENV.sqlQuery("select a from test1");
        Table result = TABLE_STREAM_ENV.sqlQuery("select count(d) from test group by d");
        //TypeConversions.fromLegacyInfoToDataType();
        //DataType array = DataTypes.ARRAY(DataTypes.STRING());

        //DataType type = table.getSchema().getTableColumn(0).get().getType();
        TABLE_STREAM_ENV.toDataStream(result1).print();
        TABLE_STREAM_ENV.toChangelogStream(result).print();
        env.execute();
    }
    private static TypeInformation<Row> getRowTypeInfo(List<FieldInfo> fieldInfos){
        int size = fieldInfos.size();
        TypeInformation[] types = new TypeInformation[size];
        String[] fieldNames = new String[size];
        for(int i=0;i<size;i++){
            switch (fieldInfos.get(i).getColumnType()){
                case "string" : types[i] = BasicTypeInfo.STRING_TYPE_INFO; break;
                case "int" : types[i] = BasicTypeInfo.INT_TYPE_INFO; break;
                case "date":types[i] = BasicTypeInfo.DATE_TYPE_INFO; break;
                case "long":types[i] = BasicTypeInfo.LONG_TYPE_INFO;break;
                case "timestamp":types[i] = SqlTimeTypeInfo.TIMESTAMP;break;
            }
            fieldNames[i] = fieldInfos.get(i).getColumnName();
        }
        return new RowTypeInfo(types,fieldNames);
    }

    public static DataType[] fromLegacyInfoToDataType(TypeInformation<?>[] typeInfo) {

        return Stream.of(typeInfo)
                .map(LegacyTypeInfoDataTypeConverter::toDataType)
                .toArray(DataType[]::new);
    }



    //我们可以采用with来注册对应的数据
    public static void tableTimeRow(StreamExecutionEnvironment env){
        DataStream<String> inputDataStream = env.socketTextStream("192.168.16.176", 1111);
        List<FieldInfo> list = new ArrayList<>();
        list.add(new FieldInfo("a","int"));
        list.add(new FieldInfo("b","string"));
        list.add(new FieldInfo("c","long"));
        RowTypeInfo rowTypeInfo = (RowTypeInfo) getRowTypeInfo(list);
        SingleOutputStreamOperator<Senior> mapOperator = inputDataStream.map(new RichMapFunction<String,Senior>() {
            @Override
            public Senior map(String value) {
                String[] split = value.split(",");
                return new Senior(Integer.parseInt(split[0]),split[1], Long.valueOf(split[2]));
            }
        }).assignTimestampsAndWatermarks(new CusWatermarkStrategy<Senior>());

        SingleOutputStreamOperator<Row> rowOperator = mapOperator.transform("map", rowTypeInfo, new StreamMap<>(new RichMapFunction<Senior, Row>() {
            @Override
            public Row map(Senior value) throws Exception {
                return Row.of(value.getAge(), value.getName(), value.getScore());
            }
        }));
        Table table = TABLE_STREAM_ENV.fromDataStream(rowOperator,$("a"),$("b"),$("c").rowtime());

        //proctime表示处理时间 这个是一个虚拟字段, rowtime 表示事件时间 类型必须是long或者timeStamp类型
        //FlinkExecutionEnvironment.TABLE_STREAM_ENV.registerTable("test",new UserActionSource(mapOperator));
        //Table table = FlinkExecutionEnvironment.TABLE_STREAM_ENV.fromDataStream(mapOperator,$("d"),$("b"),$("c").rowtime());
        //table.window(Tumble.over(lit(10).minutes()).on($("c")).as("userWindow"));
        TABLE_STREAM_ENV.createTemporaryView("test",table);
        Table table1 = TABLE_STREAM_ENV.
                sqlQuery("select TUMBLE_START(c,INTERVAL '1' SECOND),count(distinct(b)) from test group by TUMBLE(c,INTERVAL '1' SECOND)");
        //转成流
        TABLE_STREAM_ENV.toRetractStream(table1,Row.class).print();

    }


    public static class UserActionSource implements StreamTableSource<Row>, DefinedRowtimeAttributes {
        private SingleOutputStreamOperator inputStream ;


        public UserActionSource(SingleOutputStreamOperator inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public TypeInformation<Row> getReturnType() {
            String[] names = new String[]{"d","b","c"};
            TypeInformation[] types = new TypeInformation[]{Types.INT,Types.STRING,Types.INT};
            return new RowTypeInfo(types,names);
        }

        @Override
        public TableSchema getTableSchema() {
            String[] names = new String[]{"d","b","c"};
            TypeInformation[] types = new TypeInformation[]{Types.INT,Types.STRING,Types.INT};
            //TypeInformation -> DataTypes
            return TableSchema.builder().fields(names, TypeConversions.fromLegacyInfoToDataType(types)).build();
        }


        /*@org.jetbrains.annotations.Nullable
        @Override
        public String getProctimeAttribute() {
            return "c";
        }*/


        @Override
        public DataStream<Row> getDataStream(StreamExecutionEnvironment execEnv) {
            SingleOutputStreamOperator<Senior> operator = inputStream.
                    //assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(1)).withIdleness(Duration.ofSeconds(1)));
                    assignTimestampsAndWatermarks(new CusWatermarkStrategy<Senior>());
            SingleOutputStreamOperator<Row> map = operator.map(new RichMapFunction<Senior, Row>() {
                @Override
                public Row map(Senior value) throws Exception {
                    return Row.of(value.getAge(), value.getName(), value.getScore());
                }
            });
            return map;
        }

        @Override
        public List<RowtimeAttributeDescriptor> getRowtimeAttributeDescriptors() {
            RowtimeAttributeDescriptor rowtimeAttrDescr = new RowtimeAttributeDescriptor(
                    "c_time",
                    new ExistingField("user_action_time"),
                    new AscendingTimestamps());
            List<RowtimeAttributeDescriptor> listRowtimeAttrDescr = Collections.singletonList(rowtimeAttrDescr);
            return listRowtimeAttrDescr;
        }
    }


        public static void getTableCatalog(StreamExecutionEnvironment env){
        // 从socket文本流读取数据
        DataStream<String> inputDataStream = env.socketTextStream("192.168.16.176", 1111);
        List<FieldInfo> list = new ArrayList<>();
        list.add(new FieldInfo("a","string"));
        list.add(new FieldInfo("b","string"));
        list.add(new FieldInfo("c","string"));
        //row 转换
        RowTypeInfo rowTypeInfo = (RowTypeInfo) getRowTypeInfo(list);
        SingleOutputStreamOperator<Row> rowOperator = inputDataStream.transform("map", rowTypeInfo, new StreamFlatMap<String, Row>(new FlatMapFunction<String, Row>() {
            @Override
            public void flatMap(String value, Collector<Row> out) throws Exception {
                String[] split = value.split(",");
                out.collect(Row.of(split[0], split[1], split[2]));
            }
        }));
        Table table = TABLE_STREAM_ENV.fromDataStream(rowOperator,$("a"),$("b"),$("c"));
        TABLE_STREAM_ENV.createTemporaryView("test",table);
        Table table1 = TABLE_STREAM_ENV.sqlQuery("select count(1) e,b,c from test group by a,b,c");
        DataType dataType = table1.getSchema().toPhysicalRowDataType();
        System.out.println(dataType.toString());
    }

   /* public class UserActionSources implements DynamicTableSource {

        @Override
        public DynamicTableSource copy() {
            return "so";
        }

        @Override
        public String asSummaryString() {
            return null;
        }
    }*/





}
