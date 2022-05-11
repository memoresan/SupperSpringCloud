package util;

import entity.yml.BaseYml;

import java.util.Properties;
import java.util.ServiceLoader;

public class YmlUtils {
    public static Properties getProperties(){
        Properties properties = new Properties();
        ServiceLoader<BaseYml> serviceLoader = ServiceLoader.load(BaseYml.class);
        for(BaseYml service : serviceLoader) {
            properties.putAll(service.getResult());
        }
        return properties;
    }


    public static void main(String[] agrs){
        Properties properties = getProperties();
        System.out.println(properties.getProperty("spark.executor.number"));
        System.out.println(properties.getProperty("spark.executor.memory"));
        System.out.println(properties.getProperty("database.impala.url"));
        System.out.println(properties.getProperty("database.impala.userName"));
    }
}
