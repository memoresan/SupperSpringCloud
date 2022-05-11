package core;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.core.fs.Path;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.bridge.java.BatchTableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class FlinkExecutionEnvironment {
    public static StreamExecutionEnvironment EXE_ENV ;
    public static StreamTableEnvironment TABLE_STREAM_ENV;
    static{
        init();

    }

    //public static TableEnvironment TABLE_BATCH_ENV;
    public static void init(){
        getEnvironment(false);
        getStreamTableEnvironment();
        //setCheckPoint(EXE_ENV);
    }





    public static StreamExecutionEnvironment getEnvironment(boolean isBatch){
        Configuration conf = new Configuration();
        conf.setInteger(RestOptions.PORT, 8051);
        if(EXE_ENV == null){
            EXE_ENV = EXE_ENV.createLocalEnvironmentWithWebUI(conf);
            //  StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            if(isBatch){
                EXE_ENV= EXE_ENV.setRuntimeMode(RuntimeExecutionMode.BATCH);
            }else {
                EXE_ENV= EXE_ENV.setRuntimeMode(RuntimeExecutionMode.STREAMING);
            }
        }
        return EXE_ENV;
    }

    public static TableEnvironment getStreamTableEnvironment(){
        EnvironmentSettings blinkStreamSettings = EnvironmentSettings.newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();
        if(TABLE_STREAM_ENV == null){
            TABLE_STREAM_ENV = StreamTableEnvironment.create(EXE_ENV, blinkStreamSettings);
        }
        return TABLE_STREAM_ENV;
    }




    public static void setCheckPoint(StreamExecutionEnvironment streamExecutionEnvironment){
        streamExecutionEnvironment.setStateBackend(new FsStateBackend(new Path("file:///E:/checkpoint")));
        CheckpointConfig checkpointConfig = streamExecutionEnvironment.getCheckpointConfig();
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        checkpointConfig.setCheckpointInterval(1000); //两个checkpoint的间隔时间单位ms
        //CheckpointConfig.ExternalizedCheckpointCleanup 表示当作业取消的时候检查点是否也要取消
        //关闭的时候师傅保留最后一次的ck数据
        checkpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        checkpointConfig.setMinPauseBetweenCheckpoints(500);
        //检查点必须1分钟内完成
        checkpointConfig.setCheckpointTimeout(60000);
        //同一个时间只能有一个检查点，这个值是设置同一个时间最大有多少检查点，当设置后，如果检查点个数达到最大值，会等待只有检查点过期才可以再一次触发新的检查点
        checkpointConfig.setMaxConcurrentCheckpoints(1);
        //第一个参数尝试重启的次数，第二个参数是要每隔多长时间进行重启
        streamExecutionEnvironment.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,2000L));
    }
}
