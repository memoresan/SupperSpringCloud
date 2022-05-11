package entity;

public abstract class BaseEntity {
      String processId;
      String processInfoId;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessInfoId() {
        return processInfoId;
    }

    public void setProcessInfoId(String processInfoId) {
        this.processInfoId = processInfoId;
    }
}
