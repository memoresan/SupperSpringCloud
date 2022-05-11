package flinksql.cus.watermarks;

import entity.BaseEntity;
import org.apache.flink.api.common.eventtime.*;

/**
 * 相当于重新调入对应的方法
 * @param <T>
 */
public class CusWatermarkStrategyWithTimestampAssigner<T extends BaseEntity> implements WatermarkStrategy<T> {
    private final WatermarkStrategy<T> baseStrategy;
    private final TimestampAssignerSupplier<T> timestampAssigner;

    public CusWatermarkStrategyWithTimestampAssigner(WatermarkStrategy<T> baseStrategy, TimestampAssignerSupplier<T> timestampAssigner) {
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
