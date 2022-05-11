package util.log4j2;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;

@Plugin(name="var",category = StrLookup.CATEGORY)
public class CusLookup implements StrLookup {
    @Override
    public String lookup(String key) {
        return key+1;
    }

    @Override
    public String lookup(LogEvent event, String key) {
        return key+2;
    }
}
