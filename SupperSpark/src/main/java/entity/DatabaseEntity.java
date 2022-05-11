package entity;

public class DatabaseEntity {
    private String url;
    private String tableName;
    private String userName;
    private String passwd;


    public DatabaseEntity(String url, String tableName, String userName, String passwd) {
        this.url = url;
        this.tableName = tableName;
        this.userName = userName;
        this.passwd = passwd;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
}
