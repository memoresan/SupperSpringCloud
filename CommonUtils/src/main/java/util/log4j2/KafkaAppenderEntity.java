package util.log4j2;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.PluginElement;

public class KafkaAppenderEntity {
    private Property[] properties;
    private String topic;
    private boolean syncSend;

    public boolean isSyncSend() {
        return syncSend;
    }

    public void setSyncSend(boolean syncSend) {
        this.syncSend = syncSend;
    }

    public KafkaAppenderEntity(boolean syncSend,Property[] properties, String topic) {
        this.properties = properties;
        this.topic = topic;
        this.syncSend = syncSend;
    }



    public Property[] getProperties() {
        return properties;
    }

    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
