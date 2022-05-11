package util.kafka.producer;

import entity.kafka.KafkaConsumerEntity;
import entity.kafka.KafkaProducterEntity;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.checkerframework.checker.units.qual.K;
import util.kafka.consumer.ConsumerUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProducerTransaction {
    public static void main(String[] agrs){
        KafkaProducterEntity entity = new KafkaProducterEntity();
        entity.getKafkaProducerConfig().put(ProducerConfig.TRANSACTIONAL_ID_CONFIG,"my-transaction-id");
        entity.getKafkaProducerConfig().put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,"true");
        //entity.getKafkaProducerConfig().put("transaction.state.log.replication.factor","1");
        //entity.getKafkaProducerConfig().put("transaction.state.log.min.isr","1");
        KafkaProducer<String,String> kafkaProducer = ProducerUtils.init(entity);
        kafkaProducer.initTransactions();
        KafkaConsumerEntity kafkaConsumerEntity = new KafkaConsumerEntity();
        kafkaConsumerEntity.setGroupId("my-group");
        kafkaConsumerEntity.setAutoOffsetReset(false);
        kafkaConsumerEntity.getKafkaProducerConfig().put(ConsumerConfig.ISOLATION_LEVEL_CONFIG,"read_committed");
        kafkaConsumerEntity.getKafkaProducerConfig().put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");
        KafkaConsumer<String,String> kafkaConsumer = ConsumerUtils.init(kafkaConsumerEntity);
        kafkaConsumer.subscribe(Collections.singletonList("test1"));
        while(true){
            try {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMinutes(1));
                kafkaProducer.beginTransaction();
                Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    kafkaProducer.send(new ProducerRecord<String, String>("test", consumerRecord.key(), consumerRecord.value()));
                    kafkaProducer.send(new ProducerRecord<String, String>("test2", consumerRecord.key(), consumerRecord.value()));
                    map.put(new TopicPartition(consumerRecord.topic(), consumerRecord.partition()), new OffsetAndMetadata(consumerRecord.offset(), null));
                }
                //提交offset
                kafkaProducer.sendOffsetsToTransaction(map, "my-group");
                //提交事务
                kafkaProducer.commitTransaction();
            }catch (Exception e){
                e.printStackTrace();
                kafkaProducer.abortTransaction();
            }
        }

    }


}
