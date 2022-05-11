package util.function.flink;
import util.apt.function.StringFunction;
import org.apache.flink.table.functions.ScalarFunction;
import util.annotation.UdfAnnotation;

 public class StringFunctionConcat1 extends ScalarFunction
 {
    public java.lang.String eval(String arg0,String arg1){
        return StringFunction.concat1(arg0,arg1);
    }

 }
