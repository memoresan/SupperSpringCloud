package sink;

import core.FlinkExecutionEnvironment;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkFixedPartitioner;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import util.YmlUtils;

import java.util.Properties;

public class SinkKafka {
    static Properties kafkaCustomerProper = new Properties();
    static Properties kafkaProducerProper = new Properties();
    public static void kafkaSource(StreamExecutionEnvironment env) throws Exception {
        Properties properties = YmlUtils.getProperties();
        kafkaCustomerProper.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,properties.getProperty("kafka.bootstrap.servers"));
        kafkaCustomerProper.setProperty(ConsumerConfig.GROUP_ID_CONFIG,properties.getProperty("kafka.group.id"));
        FlinkKafkaConsumer<String> flinkKafkaConsumer = new FlinkKafkaConsumer<String>("test-flink",new SimpleStringSchema(),kafkaCustomerProper);
        //flinkKafkaConsumer.assignTimestampsAndWatermarks();
        DataStreamSource<String> stream = env.addSource(flinkKafkaConsumer);
        kafkaProducerProper.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,properties.getProperty("kafka.bootstrap.servers"));
        FlinkKafkaProducer<String> myProducer = new FlinkKafkaProducer<String>("flink_kafka_poc_output",
                new SimpleStringSchema(),
                kafkaProducerProper,
                new FlinkFixedPartitioner(),
                FlinkKafkaProducer.Semantic.EXACTLY_ONCE,
                10);
        stream.addSink(myProducer);
        stream.print();
        env.execute();
    }

    public static void main(String[] agrs) throws Exception {
        StreamExecutionEnvironment env = FlinkExecutionEnvironment.getEnvironment(true);
        kafkaSource(env);
    }
}

