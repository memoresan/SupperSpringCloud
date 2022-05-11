package core.util.log;

import exception.InitFaileException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log4j2Provider extends LogProvider
{

    /**
     * 
     */
    private static final String LOG4J2_CLASS_NAME = "org.apache.logging.slf4j.Log4jLoggerFactory";

    /**
     * 日志的根输出器名称
     */
    private static final String ROOT_LOGGER_NAME = "ROOT";

    private static final String WORK_FLOW = "workflow";

    /**
     * 流程日志输出对象
     */
    private final Logger workLogger;

    Log4j2Provider(Class<?> name) throws InitFaileException {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (LOG4J2_CLASS_NAME.equals(factory.getClass().getName()))
        {
            // 加载的log4j2的jar包，直接使用当前的
            workLogger = LoggerFactory.getLogger(name);
        }
        else
        {
            // 强制使用log4j2的jar包
            try
            {
                //注意现在所有的newInstance全部修改成这种
                factory = (ILoggerFactory)Class.forName(LOG4J2_CLASS_NAME).getConstructor().newInstance();
                workLogger = factory.getLogger(name.getName());
            }
            catch (Exception e)
            {
                throw new InitFaileException("can not init log4j2 factory", e);
            }
        }

    }

    @Override
    public Logger getWorkFlowLogger()
    {
        return workLogger;
    }

    @Override
    public void setWorkflowOutPath(String processDefId, String processInsId)
    {
        if (StringUtils.isBlank(processDefId))
        {
            throw new IllegalArgumentException("processDefId is null");
        }
        if (StringUtils.isEmpty(processInsId))
        {
            throw new IllegalArgumentException("processInsId is null");
        }

        String filePath = getLogPath(processDefId, processInsId);
        ThreadContext.put("logFileName", filePath);
    }

    @Override
    public void setLogLevel(String loggerName, String level)
    {
        Level one = convertLevel(level);
        if (one != null)
        {
            LoggerConfig loggerConfig = getLoggerConfig(loggerName);
            if (loggerConfig == null)
            {
                loggerConfig = new LoggerConfig(loggerName, one, true);
                getLoggerContext().getConfiguration().addLogger(loggerName, loggerConfig);
            }
            else
            {
                loggerConfig.setLevel(one);
            }
            getLoggerContext().updateLoggers();
        }
    }

    /**
     * @param level 日志级别名称
     * @return 日志级别，不存在返回null
     */
    private Level convertLevel(String level)
    {
        return StringUtils.isNotBlank(level) ? Level.getLevel(level.toUpperCase()) : null;
    }

    /**
     * @param name 日志名称
     * @return 日志配置
     */
    private LoggerConfig getLoggerConfig(String name)
    {
        if (StringUtils.isBlank(name) || ROOT_LOGGER_NAME.equals(name))
        {
            name = LogManager.ROOT_LOGGER_NAME;
        }
        return getLoggerContext().getConfiguration().getLoggers().get(name);
    }

    /**
     * @return context
     */
    private LoggerContext getLoggerContext()
    {
        return (LoggerContext) LogManager.getContext(false);
    }


}