package flink.operator;

import org.apache.flink.api.common.functions.Function;
import org.apache.flink.api.common.operators.AbstractUdfOperator;
import org.apache.flink.streaming.api.operators.AbstractUdfStreamOperator;
import org.apache.flink.util.Visitor;


public class FlinkStreamOperator extends AbstractUdfStreamOperator {
    public FlinkStreamOperator(Function userFunction) {
        super(userFunction);
    }
}
