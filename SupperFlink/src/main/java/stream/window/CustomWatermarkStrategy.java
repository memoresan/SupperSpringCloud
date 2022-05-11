package stream.window;

import entity.BaseEntity;
import entity.Senior;
import org.apache.flink.api.common.eventtime.*;

//这个类型是传入数据的类型
public class CustomWatermarkStrategy<T extends BaseEntity> implements WatermarkStrategy<T> {
    private long maxOutOfOrderness;

    public CustomWatermarkStrategy(long maxOutOfOrderness) {
        this.maxOutOfOrderness = maxOutOfOrderness;
    }

    @Override
    public WatermarkGenerator<T> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
        return new CustomWatermarkGenerator<T>(maxOutOfOrderness);
    }

    //获取时间记录的时间戳，传入到onEvent方法中的eventTimestamp,这个类可以有也可以没有，如果没有就在onEvent方法中加入逻辑
    @Override
    public TimestampAssigner<T> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
        return new CusTimestampAssigner<T>();
    }

    //外部提供自定义TimestampAssigner接口
    @Override
    public WatermarkStrategy<T> withTimestampAssigner(TimestampAssignerSupplier<T> timestampAssigner) {
        return new ComWatermarkStrategyWithTimestampAssigner<T>(this,timestampAssigner);
    }


}

class ComWatermarkStrategyWithTimestampAssigner<T> implements WatermarkStrategy<T>{
    private final WatermarkStrategy<T> baseStrategy;
    private final TimestampAssignerSupplier<T> timestampAssigner;

    public ComWatermarkStrategyWithTimestampAssigner(WatermarkStrategy<T> baseStrategy, TimestampAssignerSupplier<T> timestampAssigner) {
        this.baseStrategy = baseStrategy;
        this.timestampAssigner = timestampAssigner;
    }

    @Override
    public WatermarkGenerator<T> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
        return baseStrategy.createWatermarkGenerator(context);
    }

    @Override
    public TimestampAssigner<T> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
        return timestampAssigner.createTimestampAssigner(context);
    }
}




class CustomWatermarkGenerator<T extends BaseEntity> implements  WatermarkGenerator<T>{

    private long currentTimeStamp;
    private long maxOutOfOrderness;

    public CustomWatermarkGenerator(long maxOutOfOrderness) {
        this.maxOutOfOrderness = maxOutOfOrderness;
    }

    //当前有数据进来就执行这个方法,当然可以进行实时发送水位线
    @Override
    public void onEvent(T event, long eventTimestamp, WatermarkOutput output) {
        if(currentTimeStamp < eventTimestamp){
            currentTimeStamp = eventTimestamp;
        }
        //实时发送水位线  output.emitWatermark(new Watermark(currentTimeStamp));
    }

    //这个是周期发送水位线
    @Override
    public void onPeriodicEmit(WatermarkOutput output) {
        //默认200ms
        output.emitWatermark(new Watermark(currentTimeStamp-maxOutOfOrderness));
    }
}


class  CusTimestampAssigner<T extends BaseEntity> implements  TimestampAssigner<T>{
    @Override
    public long extractTimestamp(T element, long recordTimestamp) {
        return element.getTimestamp() * 1000;
    }
}
