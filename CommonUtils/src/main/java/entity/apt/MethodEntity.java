package entity.apt;

import java.util.List;

public class MethodEntity {
    private String methodName;
    private List<ParamEntity> methodParamType;
    private ParamEntity methodReturnType;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<ParamEntity> getMethodParamType() {
        return methodParamType;
    }

    public void setMethodParamType(List<ParamEntity> methodParamType) {
        this.methodParamType = methodParamType;
    }

    public ParamEntity getMethodReturnType() {
        return methodReturnType;
    }

    public void setMethodReturnType(ParamEntity methodReturnType) {
        this.methodReturnType = methodReturnType;
    }
}
