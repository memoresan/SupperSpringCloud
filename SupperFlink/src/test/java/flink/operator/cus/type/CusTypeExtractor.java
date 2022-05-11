package flink.operator.cus.type;

import flink.operator.cus.function.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;

public class CusTypeExtractor extends TypeExtractor {
    public static <IN, OUT> TypeInformation<OUT> getCusMapReturnTypes(
            MapFunction<IN, OUT> mapInterface,
            TypeInformation<IN> inType,
            String functionName,
            boolean allowMissing) {
        return getUnaryOperatorReturnType(
                mapInterface,
                MapFunction.class,
                0,
                1,
                NO_INDEX,
                inType,
                functionName,
                allowMissing);
    }


}
