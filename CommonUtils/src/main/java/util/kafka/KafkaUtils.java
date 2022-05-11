package util.kafka;

import entity.kafka.KafkaTopicEntity;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;
import util.yml.YmlUtils;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangtianyu0807
 */
public class KafkaUtils {
    private static final String zkPort;
    private static final int numPartitions;
    private static final short replicationFactor;

    static {
        zkPort = YmlUtils.getProperties().getProperty("kafka.bootstrap.servers");
        numPartitions = Integer.parseInt(YmlUtils.getProperties().getProperty("kafka.topic.numPartitions","1"));
        replicationFactor = Short.parseShort(YmlUtils.getProperties().getProperty("kafka.topic.replicationFactor","1"));
    }

    public static void createTopic(List<KafkaTopicEntity> topicEntitys){
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, zkPort);
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(properties);
            List<NewTopic> list = new ArrayList<>();
            topicEntitys.forEach(topicEntity -> {
                if (topicEntity.getReplicasAssignments() != null) {
                    NewTopic topic = new NewTopic(topicEntity.getTopicName(), topicEntity.getReplicasAssignments());
                    if (topicEntity.getConfigs() != null) {
                        topic.configs(topicEntity.getConfigs());
                    }
                    list.add(topic);
                } else {
                    int numberPartitions = numPartitions;
                    short replicationFactors = replicationFactor;
                    if (topicEntity.getNumPartitions() != null) {
                        numberPartitions = topicEntity.getNumPartitions();
                    }
                    if (topicEntity.getReplicationFactor() != null) {
                        replicationFactors = topicEntity.getReplicationFactor();
                    }
                    NewTopic topic = new NewTopic(topicEntity.getTopicName(), numberPartitions, replicationFactors);
                    list.add(topic);
                }
            });
            adminClient.createTopics(list);
        }finally {
            adminClient.close();
        }
    }
    public static void deleteTopic(List<KafkaTopicEntity> toicEntitys) {
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, zkPort);
        AdminClient adminClient = null;
        try{
            adminClient = AdminClient.create(properties);
            List<String> deleteTopics = toicEntitys.stream().map(x -> x.getTopicName()).collect(Collectors.toList());
            adminClient.deleteTopics(deleteTopics);
        }finally {
            adminClient.close();
        }
    }

    public static void appendTopicConfig(List<KafkaTopicEntity> toicEntitys) {
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, zkPort);
        AdminClient adminClient = null;
        try{
            adminClient = AdminClient.create(properties);
            Map<ConfigResource, Collection<AlterConfigOp>> alterTopics = toicEntitys.stream().collect(Collectors.toMap(x->{
                return new ConfigResource(ConfigResource.Type.TOPIC,x.getTopicName());
            },y->{
                List<AlterConfigOp> list = new ArrayList<>();
                y.getConfigs().forEach((String x,String e)->{
                    AlterConfigOp alterConfigOp = new AlterConfigOp(new ConfigEntry(x,e), AlterConfigOp.OpType.APPEND);
                    list.add(alterConfigOp);
                });
                return list;
            }));
            adminClient.incrementalAlterConfigs(alterTopics);
        }finally {
            Optional.of(adminClient).ifPresent(x->x.close());
        }
    }

    /** 返回所有的topic **/
    public static Set<String> listTopic() {
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, zkPort);
        AdminClient adminClient = null;
        try{
            adminClient = AdminClient.create(properties);
            Set<String> names = adminClient.listTopics().names().get();
            return names;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }  finally {
            Optional.of(adminClient).ifPresent(x->x.close());
        }
    }





}
