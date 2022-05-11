package entity.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KafkaTopicEntity {
    //topic名称
    private String topicName;
    //partition数量
    private Integer numPartitions;
    //副本个数
    private Short replicationFactor;

    private Map<Integer, List<Integer>> replicasAssignments;

    public KafkaTopicEntity(String topicName, Integer numPartitions) {
        this.topicName = topicName;
        this.numPartitions = numPartitions;
    }

    private Map<String, String> configs;



    public KafkaTopicEntity(String topicName, Integer numPartitions, Short replicationFactor, Map<Integer, List<Integer>> replicasAssignments, Map<String, String> configs) {
        this.topicName = topicName;
        this.numPartitions = numPartitions;
        this.replicationFactor = replicationFactor;
        this.replicasAssignments = replicasAssignments;
        this.configs = configs;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Integer getNumPartitions() {
        return numPartitions;
    }

    public void setNumPartitions(Integer numPartitions) {
        this.numPartitions = numPartitions;
    }

    public Short getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(Short replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public Map<Integer, List<Integer>> getReplicasAssignments() {
        return replicasAssignments;
    }

    public void setReplicasAssignments(Map<Integer, List<Integer>> replicasAssignments) {
        this.replicasAssignments = replicasAssignments;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }
}
