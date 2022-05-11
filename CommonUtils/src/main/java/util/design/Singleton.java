package util.design;

public class Singleton {
    private static Singleton singleton = new Singleton();
    private Singleton(){};
    public static Singleton  getInstance(){
        return singleton;
    }
}


class SingleTonLazy{
    //防止当第一个线程在释放的时候内存还没有感知到，导致多次加对象
    //为什么要两次if,那就是两个线程都进入了第一个if方法如果不加二次判断，第一个线程创建对象之后，第二个线程会继续创建对象
    private static volatile SingleTonLazy singleTonLazy ;
    public static  SingleTonLazy getInstance(){
        if(singleTonLazy ==  null){
            synchronized (SingleTonLazy.class){
                if(singleTonLazy == null){
                    singleTonLazy = new SingleTonLazy();
                }
            }
        }
        return singleTonLazy;
    }
}

class SingletonInner {
    private static SingleTonLazy singleTonLazy;
    private static class inner{
        private static  SingleTonLazy singleTonLazy = new SingleTonLazy();
    }
    public static SingleTonLazy getInstance(){
        return inner.singleTonLazy;
    }
}


