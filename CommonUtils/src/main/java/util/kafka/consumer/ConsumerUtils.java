package util.kafka.consumer;

import entity.kafka.KafkaConsumerEntity;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import util.yml.YmlUtils;

import java.time.Duration;
import java.util.*;

public class ConsumerUtils {
    private static String bootstrapServers;
    static {
        bootstrapServers = YmlUtils.getProperties().getProperty("kafka.bootstrap.servers","192.168.16.176:1000");
    }

    public static KafkaConsumer init(KafkaConsumerEntity entity) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                entity.getKeySerializer());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                entity.getValueSerializer());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, entity.getGroupId());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, String.valueOf(entity.getAutoOffsetReset()));
        entity.getKafkaProducerConfig().forEach((x, y) -> {
            properties.put(x, y);
        });
        return new KafkaConsumer(properties);
    }

    /**
     * 获取当前的offsets
     * @param consumer
     * @return
     */
    public static Map<TopicPartition, Long>  seekOffsets(Consumer consumer){
        //获取consumer对应的topic的状况
        Set<TopicPartition> assignment = consumer.assignment();
        //这边防止没有拉出对应的信息
        while(assignment.size()==0){
            //这里注意如果是0的话有可能啥也没拉出来，那么调用assignMent方法也啥也没有
            consumer.poll(Duration.ofMillis(100));
            assignment = consumer.assignment();
        }
        //通过topicPartition获取对应的end offset，这里面的值是提交offset的下一位 也可以获取头offset注意每次获取值有可能不同因为不断处理过期的数据
        Map<TopicPartition, Long> map = consumer.endOffsets(assignment);
        map.forEach((key,value)->{
            consumer.seek(key,value);
        });
        return map;

    }

    /**
     *
     * @param consumer
     * @param time System.currentTimeMillis()- 1*24*3600*1000 当前时间前一个天的数据的offset
     * @return
     */
    public static Map<TopicPartition, OffsetAndTimestamp>  seekOffsetForTimes(Consumer consumer,long time){
        //获取consumer对应的topic的状况
        Set<TopicPartition> assignment = consumer.assignment();
        //这边防止没有拉出对应的信息
        while(assignment.size()==0){
            //这里注意如果是0的话有可能啥也没拉出来，那么调用assignMent方法也啥也没有
            consumer.poll(Duration.ofMillis(100));
            assignment = consumer.assignment();
        }
        //通过时间获取offset,获取一天前的数据
        //先通过offsetForTimes获取一天前的offset然后在进行设置
        Map<TopicPartition,Long> mapOffset = new HashMap<>();
        assignment.stream().forEach(x->{
            mapOffset.put(x,time);
        });
        Map<TopicPartition, OffsetAndTimestamp>  map1 = consumer.offsetsForTimes(mapOffset);
        map1.forEach((key,value)->{
            consumer.seek(key,value.offset());
        });
        return map1;
    }


}
