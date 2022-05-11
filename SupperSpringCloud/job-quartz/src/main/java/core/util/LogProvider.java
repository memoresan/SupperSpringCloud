package core.util;

import core.exception.InitFaileException;
import org.slf4j.Logger;

public abstract class LogProvider
{

    /**
     * 日志文件开头
     */
    private static final String LOG_HEAD = "workflow_";

    /**
     * 日志结尾
     */
    private static final String LOG_TAIL = "_be_schedule.log";

    /**
     * 单例
     */
    private static LogProvider instance = null;

    /**
     * 获取日志提供器
     *
     * @return 日志提供器
     */
    public static LogProvider getInstance(Class<?> className) throws InitFaileException {
        if (instance == null)
        {
            synchronized (LogProvider.class)
            {
                if (instance == null)
                {
                    if (isSupportLog4j2())
                    {
                        instance = new Log4j2Provider(className);
                    }
                    else
                    {
                        //instance = new Log4jProvider();
                    }
                }
            }
        }
        return instance;
    }

    /**
     * 是否支持log4j2
     *
     * @return true表示支持
     */
    private static boolean isSupportLog4j2()
    {
        try
        {
            Class.forName("org.apache.logging.slf4j.Log4jLoggerFactory");
            return true;
        }
        catch (Exception e)
        {
            // 不需要关注异常
        }
        return false;
    }

    /**
     * 获取流程日志输出对象
     *
     * @return 日志对象
     */
    public abstract Logger getWorkFlowLogger();

    /**
     * 设置日志目录
     *
     * @param processDef 流程定义
     * @param processIns 流程实例
     */
    public abstract void setWorkflowOutPath(String processDef, String processIns);

    /**
     * 设置日志级别
     *
     * @param name 输出类名或者包路径
     * @param level 日志级别，为error,warn,info,debug
     */
    public abstract void setLogLevel(String name, String level);

    /**
     * 获取日志目录
     *
     * @param processDefId 流程定义
     * @param processInsId 流程实例
     * @return 路径
     */
    public final String getLogPath(String processDefId, String processInsId)
    {
        return processDefId + "/" + LOG_HEAD + processInsId + LOG_TAIL;
    }

}
