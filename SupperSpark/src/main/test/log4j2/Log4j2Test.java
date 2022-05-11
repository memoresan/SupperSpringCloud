package log4j2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.log4j2.LoggerUtil;

public class Log4j2Test {
    public static Logger LOGGER = LoggerUtil.getLogger();
    public static void main(String[] agrs){
        LOGGER.info("aaaa");
    }
}
