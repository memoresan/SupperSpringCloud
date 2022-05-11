package core.util.log;

import exception.InitFaileException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

public class LoggerUtil {
    public static Logger logger;
    static LogProvider logProvider;
    public static Logger getLogger(){
        if(logger == null){
            try {
               /* System.setProperty("dir", "yarn");
                System.setProperty("logName", "1");*/
                logProvider = new Log4j2Provider(LoggerUtil.class);
                logProvider.setWorkflowOutPath("yarn","aa");
                return logProvider.getWorkFlowLogger();
            } catch (InitFaileException e) {
                e.printStackTrace();
            }
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
