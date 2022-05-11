package util.jdk;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author zhangtianyu0807
 */
public class CompletableFutureUtils {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorPool = getExecutorPool();
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(5000);
                //int i = 1/0;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Thread.currentThread().getName()+":z,1,2,3,4,5";
        },executorPool);

        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(()->{
           /* try {
                //Thread.sleep(1000);
                //int i = 1/0;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            System.out.println(Thread.currentThread().getName()+":z,1,2,3,4");
            return Thread.currentThread().getName()+":z,1,2,3,4";
        },executorPool);

        CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(()->{
           /* try {
                //Thread.sleep(1000);
                //int i = 1/0;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            System.out.println(Thread.currentThread().getName()+":z,1,2,3,4,5,6");
            return Thread.currentThread().getName()+":z,1,2,3,4,5,6";
        },executorPool);
        CompletableFuture<String> success = cf.thenApplyAsync((result) -> {
            System.out.println(Thread.currentThread().getName() + ":" +result);
            return Thread.currentThread().getName() + ":" +result;
        });
        success.complete("111");
        System.out.println(success.get());
    /*    cf.whenComplete((result,ex) -> {
            if(ex == null){
                System.out.println(result);
            }else {
                System.out.println(ex.getMessage()+":"+result);
            }

        });*/

       /* CompletableFuture<Void> completableFuture = cf.thenAcceptBoth(cf1, (a, b) -> {
            System.out.println(a);
            System.out.println(b);
           // return "a:b";
        });*/
        CompletableFuture<Void> future1 = CompletableFuture.allOf(cf, cf1, cf2).whenComplete((a, b) -> {
           // System.out.println(a);
        });
        CompletableFuture<Void> future = cf.thenCompose((result) -> {
            // return result;
            return CompletableFuture.runAsync(() -> {
               // System.out.println("aaa");
            });
        });

       // System.out.println(future1.get());


        //System.out.println(Thread.currentThread().getName()+":"+success.get());

        //CompletableFuture<String> exceptionally = cf.exceptionally();
        executorPool.shutdown();
    }







    public static ExecutorService getExecutorPool(){
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("demo-pool-%d").build();
        return  new ThreadPoolExecutor(2,10,2, TimeUnit.SECONDS,new ArrayBlockingQueue<>(1000),namedThreadFactory);
    }

}
