package entity.yml;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import util.log4j2.LoggerUtil;

import java.io.*;
import java.util.Map;
import java.util.Properties;

public abstract class BaseYml {

    private static Logger LOGGER = LoggerUtil.getLogger();


    public static void getYmlByFileName(String filePath,Map<String,String> result) {
        InputStream in = null;
        try {
            File file = new File(filePath);
            in = new BufferedInputStream(new FileInputStream(file));
            Yaml props = new Yaml();
            Object obj = props.loadAs(in,Map.class);
            Map<String,Object> param = (Map<String, Object>) obj;

            for(Map.Entry<String,Object> entry:param.entrySet()){
                String key = entry.getKey();
                Object val = entry.getValue();
                if(val instanceof Map){
                    forEachYaml(key,(Map<String, Object>) val,result);
                }else{
                    result.put(key, val.toString());
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(),e);
        }finally {
            if (in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(),e);
                }
            }
        }
    }


    /**
     * 遍历yml文件，获取map集合
     * @param key_str
     * @param obj
     * @return
     */
    public static Map<String,String> forEachYaml(String key_str,Map<String, Object> obj,Map<String,String> result){
        for(Map.Entry<String,Object> entry:obj.entrySet()){
            String key = entry.getKey();
            Object val = entry.getValue();
            String str_new = "";
            if(StringUtils.isNotEmpty(key_str)){
                str_new = key_str+ "."+key;
            }else{
                str_new = key;
            }
            if(val instanceof Map){
                forEachYaml(str_new,(Map<String, Object>) val,result);
            }else{
                result.put(str_new,val.toString());
            }
        }
        return result;
    }

    public static Properties getProperties(Map<String,String> map){
        Properties properties = new Properties();
        map.forEach((x,y)->{
            properties.put(x,y);
        });
        return properties;
    }

    public abstract Properties getProperties();

    public abstract Map<String,String> getResult();

}
