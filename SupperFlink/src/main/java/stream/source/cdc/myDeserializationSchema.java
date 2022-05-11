package stream.source.cdc;

import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import io.debezium.data.Envelope;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import util.jdk.DateUtils;
import util.json.JsonUtils;

import java.util.List;


public class myDeserializationSchema implements DebeziumDeserializationSchema<String> {
     /*
    期望输出效果
    {
        db:数据库名
        tb:表名
        op:操作类型
        befort:{} 数据修改前，create操作没有该项
        after:{} 数据修改后，delete操作没有该项
    }
    */

    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<String> collector) throws Exception {
        JSONObject result = JsonUtils.getJsonObject();
        String[] split = sourceRecord.topic().split("\\.");
        result.put("db",split[1]);
        result.put("tb",split[2]);
        //获取操作类型
        Envelope.Operation operation = Envelope.operationFor(sourceRecord);
        result.put("op",operation.toString().toLowerCase());
        Struct value =(Struct)sourceRecord.value();
        JSONObject after = getValueBeforeAfter(value, "after");
        JSONObject before = getValueBeforeAfter(value, "before");
        if (after!=null){result.put("after",after);}
        if (before!=null){result.put("before",before);}
        collector.collect(JsonUtils.toJsonString(result));
    }

    @Override
    public TypeInformation<String> getProducedType() {
        return BasicTypeInfo.STRING_TYPE_INFO;
    }
    public JSONObject getValueBeforeAfter(Struct value,String type){
        long tsMs = (Long)value.get("ts_ms");
        String timestamp = DateUtils.locateDateTimeToString("yyyy-MM-dd HH:mm:ss", DateUtils.getDateTimeByMillli(tsMs));
        Struct midStr = (Struct)value.get(type);
        JSONObject result = new JSONObject();
        result.put("process_time",timestamp);
        if(midStr!=null){
            List<Field> fields = midStr.schema().fields();
            for (Field field : fields) {
                result.put(field.name(),midStr.get(field));
            }
            return result;
        }
        return null;
    }


}
