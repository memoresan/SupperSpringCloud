package util.caffeine;

import com.github.benmanes.caffeine.cache.*;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import util.log4j2.LoggerUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class CaffeineCacheUtil {
    private static Logger LOGGER = LoggerUtil.getLogger();
    public static void main(String[] args) throws ExecutionException, InterruptedException {
       // caffeinePutCase();
       /* cache.put("c1", "c1");
        //获取缓存值，如果为空，返回null
        LOGGER.info("cacheTest present： [{}] -> [{}]", k, cache.getIfPresent(k));
        //获取返回值，如果为空，则运行后面表达式，存入该缓存
        //LOGGER.info("cacheTest default： [{}] -> [{}]", k, cache.get(k, this::buildLoader));
        LOGGER.info("cacheTest present： [{}] -> [{}]", k, cache.getIfPresent(k));
        //清除缓存
        cache.invalidate(k);
        LOGGER.info("cacheTest present： [{}] -> [{}]", k, cache.getIfPresent(k));*/
       // CaffeineAsynCase();
        caffeineSyncCase();
    }

    public static void caffeinePutCase() throws InterruptedException {
        Cache<String,String> caffineCache = Caffeine.newBuilder().maximumSize(3)
                //自定义策略
                .expireAfter(new Expiry<String, String>() {
            //返回的值是当前时间过后的时间长度而不是时间点，创建缓存后的时间
             @Override
             public long expireAfterCreate(@NonNull String key, @NonNull String value, long currentTime) {
                 LOGGER.info(currentTime+" "+TimeUnit.SECONDS.toNanos(3));
                 return TimeUnit.SECONDS.toNanos(3);
             }
            //更新缓存后的时间
             @Override
             public long expireAfterUpdate(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
                 return currentTime;
             }
             //读取之后的时间
             @Override
             public long expireAfterRead(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
                 LOGGER.info(currentTime+"");
                 return currentTime;
             }
            }
        ).build();
        caffineCache.put("hello","world");
        //正常获取key对应的值，如果没有返回null
        Thread.sleep(2000);
        LOGGER.info(caffineCache.getIfPresent("hello"));
        //获取值如果没有返回方法值并将值放到缓存里面
        LOGGER.info(caffineCache.get("h",x->x+1));
        LOGGER.info(caffineCache.getIfPresent("h"));
        caffineCache.invalidate("hello");
        LOGGER.info("cacheTest present： [{}] -> [{}]", "hello", caffineCache.getIfPresent("hello"));
    }


    public static void CaffeineAsynCase() throws ExecutionException, InterruptedException {
        AsyncLoadingCache<String,String> cache = Caffeine.newBuilder().expireAfterAccess(5,TimeUnit.SECONDS).maximumSize(100)
                .buildAsync(x->{return  x+1;});

        CompletableFuture<String> h = cache.get("h");
        Thread.sleep(6000);
        LOGGER.info("h的值是{}",cache.getIfPresent("h").get());
      /*  CompletableFuture<Map<String, String>> all = cache.getAll(Arrays.asList("a", "b"));
        LOGGER.info("key a的值:{},b的值{}",all.get().get("a"),all.get().get("b"));*/
    }




    //同步加载
    public static void caffeineSyncCase() throws InterruptedException {
        LoadingCache<String, String> cache =Caffeine.newBuilder()
                //多长时间刷新一次
                .refreshAfterWrite(4,TimeUnit.SECONDS)
                //.expireAfterWrite(3,TimeUnit.SECONDS)
                .maximumSize(100)
                // .expireAfterAccess(100L, TimeUnit.SECONDS)
                .build(x->{
                    return getData(x);
                });

        String k="c1";
        System.out.println(cache.get(k));
        Thread.sleep(4000);
        cache.refresh(k);
        System.out.println(cache.getIfPresent(k));
        Thread.sleep(3000);
        System.out.println(cache.getIfPresent(k));
    }

    private static String getData(String x) {
        long l = System.currentTimeMillis();
        LOGGER.info("進入"+ l);
        return x + l;
    }

    public static void cacheListener(){
        LoadingCache<String, String> cache =Caffeine.newBuilder().removalListener(CaffeineCacheUtil::getListenerInfo).maximumSize(100).build(x->{
            return getData(x);

        });
        cache.get("1");
        cache.invalidate("1");



    }

    public static void getListenerInfo(String key,String value,RemovalCause removalCause){
        LOGGER.info("枚举值:{}",removalCause.name());
    }


}
