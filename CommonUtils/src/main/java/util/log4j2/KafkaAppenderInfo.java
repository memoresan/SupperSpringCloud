package util.log4j2;

import java.io.Serializable;

public class KafkaAppenderInfo  implements Serializable {
    private String name;
    private String age;
    private String level;
    private String threadName;

    private String topic;


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        threadName = threadName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
