package core;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ClassUtils;
import util.jdk.DateUtils;


import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;


public class FlinkStart {
    static Logger loggerFactory = LoggerFactory.getLogger(FlinkStart.class);

    public static void main(String[] agrs) throws Exception {
        //DateUtils dateUtils = (DateUtils) ClassUtils.getInstance("util.jdk.DateUtils",Thread.currentThread().getContextClassLoader());
       // System.out.println(dateUtils.getDateTimeByMillli(1638339830754L));
        //System.out.println(FlinkStart.class.getClassLoader());
        /*//System.out.println();
        File file = new File("./flinklib/log4j2.xml");
        File f = new File("");
        System.out.println(file.exists());
        System.out.println(f.getAbsolutePath());
        System.out.println(FlinkStart.class.getClassLoader().getResource("util/jdk"));*/
        System.out.println(FlinkStart.class.getClassLoader());
        System.out.println(Thread.currentThread().getContextClassLoader());
        System.out.println(Thread.currentThread().getContextClassLoader().getParent());
        System.out.println(DateUtils.getDateTimeByMillli(1638339830754L));


       /* StreamExecutionEnvironment streamExecutionEnvironment = FlinkExecutionEnvironment.EXE_ENV;
        streamExecutionEnvironment.fromCollection(Collections.singleton("1")).map(new MapFunction<String, String>() {
            @Override
            public String map(String value) throws Exception {
                LocalDateTime dateTimeByMillli = DateUtils.getDateTimeByMillli(1638339830754L);
                File file = new File("./flinklib/log4j2.xml");
                System.out.println("---file"+file.exists());
                File f1 = new File("");
                System.out.println("---filePath"+f1.getAbsolutePath());
                loggerFactory.info("hhh");
                return value;
            }
        }).print();
        streamExecutionEnvironment.execute();*/

    }

}
