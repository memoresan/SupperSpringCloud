package util.logger;

import exception.InitFaileException;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Log4j2Test {
    private static ThreadLocal<LogProvider>  log4j2Provider = new ThreadLocal<>();;
    private static Logger LOGGER;
    public static void main(String[] args) throws InitFaileException {
        ExecutorService executorService = new ScheduledThreadPoolExecutor(2);
        executorService.submit(()->{
            try {
                init(Thread.currentThread().getName());
            } catch (InitFaileException e) {
                e.printStackTrace();
            }
            LOGGER.info(Thread.currentThread().getName()+"1");
        });
        executorService.submit(()->{
            try {
                init(Thread.currentThread().getName());
                LOGGER.info(Thread.currentThread().getName()+"2");
            } catch (InitFaileException e) {
                e.printStackTrace();
            }
            }
        );
    }

    public static void init(String name) throws InitFaileException {
        log4j2Provider.set(new Log4j2Provider(Log4j2Test.class));
        log4j2Provider.get().setWorkflowOutPath("yarn",name);
        LOGGER = log4j2Provider.get().getWorkFlowLogger();
    }



}
