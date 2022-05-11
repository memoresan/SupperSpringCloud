package entity;

import java.util.List;

public class HbaseEntity extends BaseEntity {
    private String  hbaseName;
    private boolean isAutoCreateTable;
    private List<ColumnFamilyEntity> columnFamily;

    public String getHbaseName() {
        return hbaseName;
    }

    public void setHbaseName(String hbaseName) {
        this.hbaseName = hbaseName;
    }

    public boolean isAutoCreateTable() {
        return isAutoCreateTable;
    }

    public void setAutoCreateTable(boolean autoCreateTable) {
        isAutoCreateTable = autoCreateTable;
    }

    public List<ColumnFamilyEntity> getColumnFamily() {
        return columnFamily;
    }

    public void setColumnFamily(List<ColumnFamilyEntity> columnFamily) {
        this.columnFamily = columnFamily;
    }


}
