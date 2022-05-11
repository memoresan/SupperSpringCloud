package flinksql.cus.watermarks;

import entity.BaseEntity;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RuntimeContext;

public class BoundedOutOfOrdernessGenerator<T extends BaseEntity> implements WatermarkGenerator<T> {
    private long maxOutofOrderness=1000;
    private static long currentMaxTimestamp = Integer.MIN_VALUE;
    //每条数据来一次如果在这里面设置水位线就是实时水位线


    public BoundedOutOfOrdernessGenerator() {
    }

    public BoundedOutOfOrdernessGenerator(long maxOutofOrderness) {
        this.maxOutofOrderness = maxOutofOrderness;
    }

    @Override
    public void onEvent(T event, long eventTimestamp, WatermarkOutput output) {
        //取最大的时间戳
        if(eventTimestamp > currentMaxTimestamp){
            currentMaxTimestamp = eventTimestamp;
        }
        //output.emitWatermark(new Watermark(currentMaxTimestamp - maxOutofOrderness));


    }

    //周期性发送水位线 通过ExecutionConfig#getAutoWatermarkInterval() 设置周期时间
    @Override
    public void onPeriodicEmit(WatermarkOutput output) {
        output.emitWatermark(new Watermark(currentMaxTimestamp - maxOutofOrderness));
    }
}
