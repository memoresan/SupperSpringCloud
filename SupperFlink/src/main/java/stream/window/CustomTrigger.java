package stream.window;

import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.Window;

public class CustomTrigger extends Trigger {
    private boolean isTumbling;

    public CustomTrigger(boolean isTumbling) {
        this.isTumbling = isTumbling;
    }

    @Override
    public TriggerResult onElement(Object element, long timestamp, Window window, TriggerContext ctx) throws Exception {
        if(isTumbling){
            ctx.registerProcessingTimeTimer(window.maxTimestamp());
            return TriggerResult.CONTINUE;
        }else {
            long currentWatermark = ctx.getCurrentWatermark();
            if(window.maxTimestamp() <= currentWatermark){
                return TriggerResult.FIRE;
            }else {
                ctx.registerEventTimeTimer(window.maxTimestamp());
                return TriggerResult.CONTINUE;
            }
        }
    }

    @Override
    public TriggerResult onProcessingTime(long time, Window window, TriggerContext ctx) throws Exception {
        return TriggerResult.FIRE;
    }

    @Override
    public TriggerResult onEventTime(long time, Window window, TriggerContext ctx) throws Exception {
        return time == window.maxTimestamp() ? TriggerResult.FIRE : TriggerResult.CONTINUE;
    }

    @Override
    public void clear(Window window, TriggerContext ctx) throws Exception {
        ctx.deleteProcessingTimeTimer(window.maxTimestamp());
    }

    @Override
    public boolean canMerge() {
        return true;
    }

    @Override
    public void onMerge(Window window, OnMergeContext ctx) throws Exception {
        super.onMerge(window, ctx);
    }
}
