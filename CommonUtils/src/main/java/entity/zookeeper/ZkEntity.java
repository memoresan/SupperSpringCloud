package entity.zookeeper;

import org.apache.zookeeper.data.Stat;

import java.util.Arrays;
import java.util.List;

public class ZkEntity {
    //数据
    private byte[] data;
    //元数据信息
    private Stat stat;
    //子节点路径
    List<String> childNodePaths;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Stat getStat() {
        return stat;
    }

    public void setStat(Stat stat) {
        this.stat = stat;
    }

    public List<String> getChildNodePaths() {
        return childNodePaths;
    }

    public void setChildNodePaths(List<String> childNodePaths) {
        this.childNodePaths = childNodePaths;
    }

    @Override
    public String toString() {
        return "ZkEntity{" +
                "data=" + Arrays.toString(data) +
                ", stat=" + stat +
                ", childNodePaths=" + childNodePaths +
                '}';
    }
}
