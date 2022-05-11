package core.entity;

import org.apache.hadoop.yarn.api.records.YarnApplicationState;

public class YarnEntity {

    private String appName;
    private String host;
    private String startTime;
    private String finishTime;
    private YarnApplicationState yarnApplicationState;

    public YarnApplicationState getYarnApplicationState() {
        return yarnApplicationState;
    }

    public void setYarnApplicationState(YarnApplicationState yarnApplicationState) {
        this.yarnApplicationState = yarnApplicationState;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }
}

