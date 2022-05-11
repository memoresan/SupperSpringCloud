package util.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.Map;

public class ConsumerRebalance<K,V> implements ConsumerRebalanceListener {

    private Consumer<K,V> consumer;
    /**
     * 我们需要将 offset map 进行提交
     */
    private Map<TopicPartition, OffsetAndMetadata> map;

    public ConsumerRebalance(Consumer<K, V> consumer, Map<TopicPartition, OffsetAndMetadata> map) {
        this.consumer = consumer;
        this.map = map;
    }

    /**
     * 在均衡开始之前和消费者停止消费之后被调用，可以进行offset提交防止没有offset
     * 这个时候原来的offset没有改变
     **/
    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        consumer.commitSync(map);
        map.clear();

    }

    /**
     * 重新分配之后和消费者开始读取之前
     * @param partitions
     */
    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

    }

    @Override
    public void onPartitionsLost(Collection<TopicPartition> partitions) {
        onPartitionsRevoked(partitions);
    }
}
