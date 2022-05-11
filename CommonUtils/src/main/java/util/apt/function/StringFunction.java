package util.apt.function;

import util.annotation.FunctionAnnotation;
import util.annotation.UdfAnnotation;


@UdfAnnotation(udfType = "flink")
public class StringFunction {
    @FunctionAnnotation()
    public static String concat(String a, String b){
        return a+b;
    }

    @FunctionAnnotation()
    public static String concat1(String a, String b){
        return a+b;
    }
}
