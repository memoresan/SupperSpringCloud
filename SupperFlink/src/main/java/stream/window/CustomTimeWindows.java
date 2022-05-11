package stream.window;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner;
import org.apache.flink.streaming.api.windowing.assigners.WindowStagger;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CustomTimeWindows extends WindowAssigner<Object, TimeWindow> {

    private boolean isTumbling;
    private Long staggerOffset;
    //窗口大小
    private long size;
    //设置的时间差
    private long globalOffset;

    private final WindowStagger windowStagger;

    private CustomTimeWindows(boolean isTumbling, long size, long globalOffset, WindowStagger windowStagger) {
        this.isTumbling = isTumbling;
        this.size = size;
        this.globalOffset = globalOffset;
        this.windowStagger = windowStagger;
    }

    //每一个元素都会分配到对应的window窗口中,如果offset是正 那么相当于时间向左移动offset单位 如果offset为负相当于向右移动
    @Override
    public Collection<TimeWindow> assignWindows(Object element, long timestamp, WindowAssignerContext context) {
        if(isTumbling){
            timestamp = context.getCurrentProcessingTime();
        }

        if (timestamp > Long.MIN_VALUE) {
            if (staggerOffset == null) {
                staggerOffset =
                        windowStagger.getStaggerOffset(context.getCurrentProcessingTime(), size);
            }

            // Long.MIN_VALUE is currently assigned when no timestamp is present
            long start = TimeWindow.getWindowStartWithOffset(
                            timestamp, (globalOffset + staggerOffset) % size, size);
            return Collections.singletonList(new TimeWindow(start, start + size));
        } else {
            throw new RuntimeException(
                    "Record has Long.MIN_VALUE timestamp (= no timestamp marker). "
                            + "Is the time characteristic set to 'ProcessingTime', or did you forget to call "
                            + "'DataStream.assignTimestampsAndWatermarks(...)'?");
        }
    }

    //自定义trigger
    @Override
    public Trigger getDefaultTrigger(StreamExecutionEnvironment env) {
        return new CustomTrigger(isTumbling);
    }

    @Override
    public TypeSerializer getWindowSerializer(ExecutionConfig executionConfig) {
        return new TimeWindow.Serializer();
    }

    @Override
    public boolean isEventTime() {
        return !isTumbling;
    }

    //初始化加载
    public static CustomTimeWindows of(boolean isTumbling, Time size){
        return new CustomTimeWindows (isTumbling,size.toMilliseconds(),0,WindowStagger.ALIGNED);
    }

    public static CustomTimeWindows of(boolean isTumbling,Time size,long globalOffset){
        return new CustomTimeWindows (isTumbling,size.toMilliseconds(),globalOffset,WindowStagger.ALIGNED);
    }
}
