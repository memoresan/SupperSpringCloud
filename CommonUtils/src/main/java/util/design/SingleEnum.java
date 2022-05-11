package util.design;

public enum SingleEnum {
    INSTANCE;
    private SingletonInner singletonInner;
    private SingleEnum(){
        singletonInner = new SingletonInner();
    }
    public static SingletonInner getInstance(){
      return   SingleEnum.INSTANCE.singletonInner;
    }
}
