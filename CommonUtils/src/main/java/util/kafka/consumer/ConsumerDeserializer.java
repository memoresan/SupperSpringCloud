package util.kafka.consumer;

import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

/**
 * @author zhangtianyu0807
 */
public class ConsumerDeserializer<T> implements Deserializer<T> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try {
            ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
            return (T) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() {

    }
}
