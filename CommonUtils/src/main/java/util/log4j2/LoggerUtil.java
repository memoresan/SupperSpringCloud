package util.log4j2;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;

public class LoggerUtil {
    public static Logger logger;
    static LogProvider logProvider;
    public static Logger getLogger()  {
        if(logger == null){
            System.setProperty("dir", "yarn");
            System.setProperty("logName", "1");
            try {
                logProvider = new Log4j2Provider(LoggerUtil.class);
            } catch (Exception e) {
              e.printStackTrace();
            }
            logProvider.setWorkflowOutPath("yarn","1");
            return logProvider.getWorkFlowLogger();
        }
        return logger;
    }

    public static void setLevel(String name,String level){
        if(StringUtils.isBlank(name)){
            logProvider.setLogLevel(LoggerUtil.class.getName(),level);
        }else{

        }
    }
}
