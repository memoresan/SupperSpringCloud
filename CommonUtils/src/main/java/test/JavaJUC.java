package test;

import org.ehcache.core.spi.time.SystemTimeSource;
import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.atomic.LongAdder;

public class JavaJUC {
    public static void main(String[] agrs) throws ExecutionException, InterruptedException {

        /*ExecutorService executorService = Executors.newCachedThreadPool();
        Future future = executorService.submit(JavaJUC::run1);
        executorService.shutdown();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            shutdownThreadPoolGraceFully(executorService);
        }));*/

        LongAdder longAdder = new LongAdder();

        //ThreadLocal threadLocal = new ThreadLocal();
        //executorService.shutdown();
        /*DelayQueue<CusDelayQueue> delayQueue = new DelayQueue();
        delayQueue.offer(new CusDelayQueue("task1",10000));
        delayQueue.offer(new CusDelayQueue("task2",900));
        delayQueue.offer(new CusDelayQueue("task3",1900));
        delayQueue.offer(new CusDelayQueue("task4",5900));
        while (delayQueue.peek() != null) {
            Delayed take = delayQueue.take();
            System.out.println(take);
        }*/
    }

    static class User{
        volatile int age;

        public User(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "User{" +
                    "age=" + age +
                    '}';
        }
    }


    static class CusDelayQueue implements Delayed{

        private String name;
        private long start = System.currentTimeMillis();
        private long time ;

        public CusDelayQueue(String name, long time) {
            this.name = name;
            this.time = time;
        }

        //需要实现的接口，获得延迟时间   用过期时间-当前时间
        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return unit.convert((start+time) - System.currentTimeMillis(),TimeUnit.MILLISECONDS);
        }

        /**
         * 用于比较
         * 如果返回值是大于0的证明 传入的参数在当前参数的前面。
         * @param o
         * @return
         */
        @Override
        public int compareTo(@NotNull Delayed o) {
            CusDelayQueue delayQueue = (CusDelayQueue) o;
            return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public String toString() {
            return "CusDelayQueue{" +
                    "name='" + name + '\'' +
                    ", start=" + start +
                    ", time=" + time +
                    '}';
        }
    }

    public static void run1(){

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //int i = 1/0;
        System.out.println(Thread.currentThread().getName());
    }

    public static String run2(){
        System.out.println(Thread.currentThread().getName());
        return "1";
    }

    static public class CusThreadFactory implements ThreadFactory{
        static AtomicInteger threadNo = new AtomicInteger(1);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            String threadNode = "xxx" + threadNo.get();
            threadNo.incrementAndGet();
            Thread thread = new Thread(r,threadNode);
            thread.setPriority(5);
            return thread;
        }
    }


    public static void shutdownThreadPoolGraceFully(ExecutorService threadPool){
        if(!(threadPool instanceof ExecutorService) || threadPool.isTerminated()){
            return ;
        }
        try{
            threadPool.shutdown();
        }catch (Exception e){
            return;
        }
        try{
            if(!threadPool.awaitTermination(60,TimeUnit.SECONDS)){
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!threadPool.isTerminated()){
            for(int i=0;i<1000;i++){
                try {
                    if(threadPool.awaitTermination(10,TimeUnit.SECONDS)){
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            threadPool.shutdownNow();

        }
    }
}


