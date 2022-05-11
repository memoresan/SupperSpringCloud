package flinksql.cus.watermarks;

import entity.BaseEntity;
import org.apache.flink.api.common.eventtime.*;

public class CusWatermarkStrategy<T extends BaseEntity> implements WatermarkStrategy<T> {
    //生成水位线的
    @Override
    public WatermarkGenerator<T> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
        return new BoundedOutOfOrdernessGenerator();
    }
    //获取时间戳的
    @Override
    public TimestampAssigner<T> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
        return new CusTimestampAssigner<T>();
    }

    @Override
    public WatermarkStrategy<T> withTimestampAssigner(TimestampAssignerSupplier<T> timestampAssigner) {
        return new CusWatermarkStrategyWithTimestampAssigner<>(this,timestampAssigner);
    }
}
