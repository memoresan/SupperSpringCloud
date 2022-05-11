package flink.operator.cus.stream;

import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.streaming.api.operators.AbstractUdfStreamOperator;
import org.apache.flink.streaming.api.operators.TwoInputStreamOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;

public class CusCoStreamMap<IN1, IN2, OUT>
        extends AbstractUdfStreamOperator<OUT, CoMapFunction<IN1, IN2, OUT>>
        implements TwoInputStreamOperator<IN1, IN2, OUT> {

    public CusCoStreamMap(CoMapFunction<IN1, IN2, OUT> userFunction) {
        super(userFunction);
    }

    @Override
    public void processElement1(StreamRecord<IN1> element) throws Exception {

    }

    @Override
    public void processElement2(StreamRecord<IN2> element) throws Exception {

    }


}
