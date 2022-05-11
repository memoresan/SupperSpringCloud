package util.kafka.producer;

import entity.kafka.KafkaProducterEntity;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import util.yml.YmlUtils;

import java.util.Properties;

public class ProducerUtils {
    private static String zkPort;
    static {
        zkPort = YmlUtils.getProperties().getProperty("kafka.bootstrap.servers","192.168.16.176:1000");
    }

    public static KafkaProducer init(KafkaProducterEntity entity){
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, zkPort);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                entity.getKeySerializer());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                entity.getValueSerializer());
        entity.getKafkaProducerConfig().forEach((x,y)->{
            properties.put(x,y);
        });
        return new KafkaProducer(properties);

    }


















}
