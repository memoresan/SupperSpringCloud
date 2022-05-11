package util.log4j2;

import org.apache.commons.lang.text.StrLookup;
import org.slf4j.Logger;
import util.json.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Log4j2Test {
    private static ThreadLocal<LogProvider>  log4j2Provider = new ThreadLocal<>();;
    private static Logger LOGGER = LoggerUtil.getLogger();
    public static void main(String[] args) throws Exception {
       /* ExecutorService executorService = new ScheduledThreadPoolExecutor(2);
        executorService.submit(()->{
            try {
                init(Thread.currentThread().getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOGGER.info(Thread.currentThread().getName()+"1");
        });
        executorService.submit(()->{
            try {
                init(Thread.currentThread().getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOGGER.info(Thread.currentThread().getName()+"2");

            }
        );*/
        init("123");
        KafkaAppenderInfo kafkaAppenderInfo = new KafkaAppenderInfo();
        kafkaAppenderInfo.setTopic("log-test1");
        kafkaAppenderInfo.setName("${var:logFileName3}");
        LOGGER.info(JsonUtils.objectToJsonString(kafkaAppenderInfo));
    }

    public static void init(String name) throws Exception {
        log4j2Provider.set(new Log4j2Provider(Log4j2Test.class));
        log4j2Provider.get().setWorkflowOutPath("yarn",name);
        LOGGER = log4j2Provider.get().getWorkFlowLogger();


    }



}
