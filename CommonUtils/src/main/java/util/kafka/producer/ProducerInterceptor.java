package util.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

public class ProducerInterceptor<K, V> implements org.apache.kafka.clients.producer.ProducerInterceptor<K, V>{
    @Override
    public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
        return record;
    }
    //1.每一条记录会在failback之前进入此方法，可以进行保错和处理   这个方法优先于callback执行
    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        System.out.println("进入拦截器回调");
    }

    @Override
    public void close() {
        System.out.println("关闭");
    }
    //最先进入的方法
    @Override
    public void configure(Map<String, ?> configs) {
        System.out.println("进入1");
    }
}
