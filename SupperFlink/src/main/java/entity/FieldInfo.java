package entity;

public class FieldInfo {
    private String columnName;
    private String columnType;

    public String getColumnName() {
        return columnName;
    }

    public FieldInfo(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }
}
