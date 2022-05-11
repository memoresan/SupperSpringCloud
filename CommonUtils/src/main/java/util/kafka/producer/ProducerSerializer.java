package util.kafka.producer;



import org.apache.kafka.common.serialization.Serializer;
import util.jdk.ClassUtils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class ProducerSerializer<T> implements Serializer<T> {
    private String encoding ="UTF-8";
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        //isKey 使用来确认编码类型
        String proprertyName = isKey?"key.serializer.encoding":"value.serializer.encoding";
        Object encodingValue = configs.get(proprertyName);
        if(encodingValue == null){
            encodingValue = configs.get("serializer.encoding");
        }
        if(encodingValue != null && encodingValue instanceof String){
            encoding = (String)encodingValue;
        }
        //System.out.println("进入");
    }

    @Override
    public byte[] serialize(String topic, T data) {
        //System.out.println("序列化器进入:"+data);
        try {
            return ClassUtils.serializeJavaClass(data);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public void close() {

    }
}
