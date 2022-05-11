package util.presto.udf;


import io.prestosql.spi.block.BlockBuilder;
import io.prestosql.spi.function.*;
import io.prestosql.spi.type.DoubleType;
import io.prestosql.spi.type.StandardTypes;

@AggregationFunction("myudaf")
@Description("我的自定义聚合函数，实现计算平均数")
public class MyUdaf {
    @InputFunction
    public static void input(LongAndDoubleState state, @SqlType(StandardTypes.DOUBLE) double value){
        //在每个worker中执行，来一条数据执行一次
        state.setLong(state.getLong()+1);
        state.setDouble(state.getDouble()+value);
    }

    @CombineFunction
    public static void combine(LongAndDoubleState state,LongAndDoubleState state2){
        //将每个worker节点处理完成的结果进行聚合
        state.setLong(state.getLong()+state2.getLong());
        state.setDouble(state.getDouble()+state2.getDouble());
    }

    //聚合数据注释
    @OutputFunction(StandardTypes.DOUBLE)
    public static void output(LongAndDoubleState state, BlockBuilder out) {
        //最终输出结果到一个 BlockBuilder。
        long count = state.getLong();
        if (count == 0) {
            out.appendNull();
        } else {
            double value = state.getDouble();
            DoubleType.DOUBLE.writeDouble(out, value / count);
        }
    }
}
