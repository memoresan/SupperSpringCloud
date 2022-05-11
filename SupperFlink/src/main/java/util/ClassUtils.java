package util;

import org.apache.poi.util.SystemOutLogger;

import java.net.URLClassLoader;
import java.util.Arrays;

public class ClassUtils {

    public static Object getInstance(String className,ClassLoader classLoader) throws Exception {
        return classLoader.loadClass(className).newInstance();
    }
}


