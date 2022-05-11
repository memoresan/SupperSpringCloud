package entity.yml;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DatabaseYml extends BaseYml {
    private static Map<String,String> result = new HashMap<>();
    @Override
    public  Properties getProperties(){
        String path = DatabaseYml.class.getClassLoader().getResource("database.yml").getPath();
        getYmlByFileName(path,result);
        return getProperties(result);
    }
    @Override
    public Map<String,String> getResult(){
        String path = DatabaseYml.class.getClassLoader().getResource("database.yml").getPath();
        getYmlByFileName(path,result);
        return result;
    }

}
