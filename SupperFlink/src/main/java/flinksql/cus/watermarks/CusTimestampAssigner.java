package flinksql.cus.watermarks;

import entity.BaseEntity;
import org.apache.flink.api.common.eventtime.TimestampAssigner;

/**
 * 获取时间戳的
 */
public class CusTimestampAssigner<T extends BaseEntity> implements TimestampAssigner<T> {
    @Override
    public long extractTimestamp(T element, long recordTimestamp) {
        return element.getTimestamp() * 1000;
    }
}
