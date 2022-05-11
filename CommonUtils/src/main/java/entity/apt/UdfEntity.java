package entity.apt;

import java.util.List;

public class UdfEntity {
    //包名
    private String packageName;
    //类名
    private String className;
    //方法名
    private MethodEntity method;
    //变量名称
    private List<String> fields;

    private String orginClassPath;

    public String getOrginClassPath() {
        return orginClassPath;
    }

    public void setOrginClassPath(String orginClassPath) {
        this.orginClassPath = orginClassPath;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public MethodEntity getMethod() {
        return method;
    }

    public void setMethod(MethodEntity method) {
        this.method = method;
    }
}
