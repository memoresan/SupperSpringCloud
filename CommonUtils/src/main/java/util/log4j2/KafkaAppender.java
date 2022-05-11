package util.log4j2;

import com.alibaba.fastjson.JSONObject;
import entity.kafka.KafkaProducterEntity;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaManager;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import util.jdk.ClassUtils;
import util.json.JsonUtils;
import util.kafka.producer.ProducerUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Plugin(name = "MyKafka", category = "Core", elementType = "appender", printObject = true)
public class KafkaAppender extends AbstractAppender {
    private static final long serialVersionUID = 1L;

/*    @PluginFactory
    public static KafkaAppender createAppender(
            @PluginElement("Layout") final Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @Required(message = "No name provided for KafkaAppender") @PluginAttribute("name") final String name,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions,
            @Required(message = "No topic provided for KafkaAppender") @PluginAttribute("topic") final String topic,
            @PluginElement("Properties") final Property[] properties) {

        final KafkaManager kafkaManager = new KafkaManager(getConfiguration().getLoggerContext(),name, topic, properties);
        return new KafkaAppender(name, layout, filter, ignoreExceptions, kafkaManager);
    }*/


    @PluginBuilderFactory
    public static <B extends KafkaAppender.Builder<B>> B newBuilder() {
        return new KafkaAppender.Builder<B>().asBuilder();
    }

    public static class Builder<B extends KafkaAppender.Builder<B>> extends AbstractAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<KafkaAppender> {

        @PluginAttribute("topic")
        private String topic;

        @PluginAttribute(value = "syncSend", defaultBoolean = true)
        private boolean syncSend;

        @PluginElement("Properties")
        private Property[] properties;

        @SuppressWarnings("resource")
        @Override
        public KafkaAppender build() {
            //loggerContext.addFilter();
            KafkaAppenderEntity kafkaAppenderEntity = new KafkaAppenderEntity(syncSend,properties,topic);
            return new KafkaAppender(getName(), getLayout(), getFilter(), isIgnoreExceptions(), kafkaAppenderEntity);
        }

        public String getTopic() {
            return topic;
        }

        public Property[] getProperties() {
            return properties;
        }

        public B setTopic(String topic) {
            this.topic = topic;
            return asBuilder();
        }

        public B setSyncSend(boolean syncSend) {
            this.syncSend = syncSend;
            return asBuilder();
        }

        public B setProperties(Property[] properties) {
            this.properties = properties;
            return asBuilder();
        }
    }

    private Map<String,KafkaProducer<String,KafkaAppenderInfo>> managerMap = new ConcurrentHashMap<>();
    private final KafkaAppenderEntity  kafkaAppenderEntity;

    public KafkaAppender(String name,Layout<? extends Serializable> layout,  Filter filter, boolean ignoreExceptions, KafkaAppenderEntity kafkaAppenderEntity) {
        super(name, filter, layout, ignoreExceptions);
        this.kafkaAppenderEntity = kafkaAppenderEntity;
    }

    @Override
    public void append(LogEvent event) {
        String messgae = event.getMessage().getFormattedMessage();
        KafkaAppenderInfo kafkaAppenderInfo = (KafkaAppenderInfo)JsonUtils.toStringObject(messgae,KafkaAppenderInfo.class);
        kafkaAppenderInfo.setLevel(event.getLevel().name());
        kafkaAppenderInfo.setThreadName(event.getThreadName());
        kafkaAppenderInfo.setTime(event.getTimeMillis());
        String topic = kafkaAppenderInfo.getTopic();
        if(topic == null){
            topic = kafkaAppenderEntity.getTopic();
        }
        KafkaProducer<String,KafkaAppenderInfo> kafkaManager = managerMap.get(topic);
        if(kafkaManager == null){
            KafkaProducterEntity producterEntity = new KafkaProducterEntity();
            producterEntity.setKeySerializer(StringSerializer.class.getName());
            producterEntity.setValueSerializer(KafkaAppenderSerializer.class.getName());
            producterEntity.setKafkaProducerConfig(Arrays.stream(kafkaAppenderEntity.getProperties()).collect(Collectors.toMap(x->x.getName(),x->x.getValue())));
            kafkaManager = ProducerUtils.init(producterEntity);
            managerMap.put(topic,kafkaManager);
        }
        if (event.getLoggerName().startsWith("org.apache.kafka")) {
            LOGGER.warn("Recursive logging from [{}] for appender [{}].", event.getLoggerName(), getName());
        } else {

            try {
                if(kafkaAppenderEntity.isSyncSend()){
                    Future<RecordMetadata> send = kafkaManager.send(new ProducerRecord<String, KafkaAppenderInfo>(topic, null, kafkaAppenderInfo));
                    send.get();
                }else{
                    kafkaManager.send(new ProducerRecord<String, KafkaAppenderInfo>(topic, null, kafkaAppenderInfo), new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata metadata, Exception exception) {
                            if(exception != null){
                                exception.printStackTrace();
                            }
                        }
                    });
                }


            } catch (final Exception e) {
                e.printStackTrace();
               // LOGGER.error("Unable to write to Kafka [{}] for appender [{}].", kafkaManager.getName(), getName(), e);
                throw new AppenderLoggingException("Unable to write to Kafka in appender: " + e.getMessage(), e);
            }
        }
    }


    @Override
    public void start() {
        super.start();
        //manager.startup();
    }

    @Override
    public boolean stop(long timeout, TimeUnit timeUnit) {
        return super.stop(timeout, timeUnit);
    }
}
