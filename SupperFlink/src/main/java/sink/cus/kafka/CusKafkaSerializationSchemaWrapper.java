package sink.cus.kafka;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.streaming.connectors.kafka.KafkaContextAware;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.annotation.Nullable;

public class CusKafkaSerializationSchemaWrapper<T>
        implements KafkaSerializationSchema<T>, KafkaContextAware<T> {

    private final FlinkKafkaPartitioner<T> partitioner;
    private final SerializationSchema<T> serializationSchema;
    private final String topic;
    private boolean writeTimestamp;

    private int[] partitions;
    private int parallelInstanceId;
    private int numParallelInstances;

    public CusKafkaSerializationSchemaWrapper(
            String topic,
            FlinkKafkaPartitioner<T> partitioner,
            boolean writeTimestamp,
            SerializationSchema<T> serializationSchema) {
        this.partitioner = partitioner;
        this.serializationSchema = serializationSchema;
        this.topic = topic;
        this.writeTimestamp = writeTimestamp;
    }

    @Override
    public void open(SerializationSchema.InitializationContext context) throws Exception {
        serializationSchema.open(context);
        if (partitioner != null) {
            partitioner.open(parallelInstanceId, numParallelInstances);
        }
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(T element, @Nullable Long timestamp) {
        byte[] serialized = serializationSchema.serialize(element);
        final Integer partition;
        if (partitioner != null) {
            partition = partitioner.partition(element, null, serialized, topic, partitions);
        } else {
            partition = null;
        }

        final Long timestampToWrite;
        if (writeTimestamp) {
            timestampToWrite = timestamp;
        } else {
            timestampToWrite = null;
        }

        return new ProducerRecord<>(topic, partition, timestampToWrite, null, serialized);
    }

    @Override
    public String getTargetTopic(T element) {
        return topic;
    }

    @Override
    public void setPartitions(int[] partitions) {
        this.partitions = partitions;
    }

    @Override
    public void setParallelInstanceId(int parallelInstanceId) {
        this.parallelInstanceId = parallelInstanceId;
    }

    @Override
    public void setNumParallelInstances(int numParallelInstances) {
        this.numParallelInstances = numParallelInstances;
    }

    public void setWriteTimestamp(boolean writeTimestamp) {
        this.writeTimestamp = writeTimestamp;
    }
}
