package entity.kafka;

import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

public class KafkaProducterEntity {
    private String keySerializer = StringSerializer.class.getName();
    private String valueSerializer = StringSerializer.class.getName();
    private Map<String,String> kafkaProducerConfig = new HashMap<>();


    public Map<String, String> getKafkaProducerConfig() {
        return kafkaProducerConfig;
    }

    public void setKafkaProducerConfig(Map<String, String> kafkaProducerConfig) {
        this.kafkaProducerConfig = kafkaProducerConfig;
    }

    public String getKeySerializer() {
        return keySerializer;
    }

    public void setKeySerializer(String keySerializer) {
        this.keySerializer = keySerializer;
    }

    public String getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(String valueSerializer) {
        this.valueSerializer = valueSerializer;
    }
}
