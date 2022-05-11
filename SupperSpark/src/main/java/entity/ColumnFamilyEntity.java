package entity;

import java.util.List;

public class ColumnFamilyEntity {
    List<String> columnNames;

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }
}
