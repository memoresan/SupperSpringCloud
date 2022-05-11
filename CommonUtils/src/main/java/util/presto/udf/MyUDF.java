package util.presto.udf;


import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import io.prestosql.spi.function.Description;
import io.prestosql.spi.function.ScalarFunction;
import io.prestosql.spi.function.SqlType;
import io.prestosql.spi.type.StandardTypes;

public class MyUDF {
    //1.自定义function名称
    @ScalarFunction("myudf")
    //函数的描述
    @Description("转换字母大写为小写")
    //指定函数的返回类型，字符串类型必须返回Slice,
    @SqlType(StandardTypes.VARCHAR)
    public static Slice lowercase(@SqlType(StandardTypes.VARCHAR) Slice in)
    {
        String argument = in.toStringUtf8();
        return Slices.utf8Slice(argument.toLowerCase());
    }
    

}
