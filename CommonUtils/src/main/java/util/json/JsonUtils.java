package util.json;

import com.alibaba.fastjson.JSONObject;

public class JsonUtils {

    public static JSONObject  getJsonObject(){
        return new JSONObject();
    }

    /**
     * 將jsonObject转成jsonString
     * @param jsonObject
     */
    public static String toJsonString(JSONObject jsonObject){
        return jsonObject.toJSONString();
    }

    public static JSONObject toStringJson(String  str){
        return (JSONObject) JSONObject.parse(str);
    }

    public static Object toStringObject(String  str,Class<?> c){
        return JSONObject.parseObject(str,c);
    }

    public static String objectToJsonString(Object o){
        return JSONObject.toJSONString(o);
    }

}
