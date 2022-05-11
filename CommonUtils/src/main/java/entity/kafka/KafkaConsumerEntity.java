package entity.kafka;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

public class KafkaConsumerEntity {
    private String keySerializer = StringDeserializer.class.getName();
    private String valueSerializer = StringDeserializer.class.getName();
    private Map<String,String> kafkaProducerConfig = new HashMap<>();
    private String groupId;
    private boolean autoOffsetReset = false;


    public Boolean getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public void setAutoOffsetReset(Boolean autoOffsetReset) {
        this.autoOffsetReset = autoOffsetReset;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

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
