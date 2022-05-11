package flink.operator;


import flink.operator.cus.function.MapFunction;
import flink.operator.cus.stream.CusStreamMap;
import flink.operator.cus.type.CusTypeExtractor;
import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.dag.Transformation;
import org.apache.flink.api.java.Utils;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.streaming.api.operators.SimpleOperatorFactory;
import org.apache.flink.streaming.api.operators.StreamOperatorFactory;
import org.apache.flink.streaming.api.transformations.OneInputTransformation;

public class CusDataStream<T> extends SingleOutputStreamOperator<T> {

    /**
     * Create a new {@link DataStream} in the given execution environment with partitioning set to
     * forward by default.
     *
     * @param environment    The StreamExecutionEnvironment
     * @param transformation
     */
    public CusDataStream(StreamExecutionEnvironment environment, Transformation transformation) {
        super(environment, transformation);
    }

    public <R> SingleOutputStreamOperator<R> cusMap(MapFunction<T, R> mapper) {

        TypeInformation<R> outType =
                CusTypeExtractor.getCusMapReturnTypes(
                        clean(mapper), getType(), Utils.getCallLocationName(), true);

        return cusMap(mapper, outType);
    }
    public <R> SingleOutputStreamOperator<R> cusMap(
            MapFunction<T, R> mapper, TypeInformation<R> outputType) {
        return cusTransform("CusMap", outputType, new CusStreamMap<>(clean(mapper)));
    }


    @PublicEvolving
    public <R> SingleOutputStreamOperator<R> cusTransform(
            String operatorName,
            TypeInformation<R> outTypeInfo,
            OneInputStreamOperator<T, R> operator) {

        return cusDoTransform(operatorName, outTypeInfo, SimpleOperatorFactory.of(operator));
    }


    protected <R> CusDataStream<R> cusDoTransform(
            String operatorName,
            TypeInformation<R> outTypeInfo,
            StreamOperatorFactory<R> operatorFactory) {

        // read the output type of the input Transform to coax out errors about MissingTypeInfo
        transformation.getOutputType();

        OneInputTransformation<T, R> resultTransform =
                new OneInputTransformation<>(
                        this.transformation,
                        operatorName,
                        operatorFactory,
                        outTypeInfo,
                        environment.getParallelism());

        @SuppressWarnings({"unchecked", "rawtypes"})
        CusDataStream<R> returnStream =
                new CusDataStream(environment, resultTransform);

        getExecutionEnvironment().addOperator(resultTransform);

        return returnStream;
    }
}
