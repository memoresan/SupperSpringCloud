package util.zookeeper;

import entity.zookeeper.ZkEntity;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import util.yml.YmlUtils;

import java.util.Arrays;
import java.util.List;

public class ZKUtils {
   public static CuratorFramework client;
   static{
        String zkPort= YmlUtils.getProperties().getProperty("zookeeper.port");
        String ip= YmlUtils.getProperties().getProperty("zookeeper.ip");
        //策略 表示初始睡眠1000秒 最大失败3次，每一次间隔睡眠5s
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(10000, 10, 50000);
        client = CuratorFrameworkFactory.builder()
                //加入这个创建带有权限的目录不需要 withAcl
                /*.aclProvider(new ACLProvider() {
                    @Override
                    public List<ACL> getDefaultAcl() {
                        return ZooDefs.Ids.OPEN_ACL_UNSAFE;
                    }

                    @Override
                    public List<ACL> getAclForPath(String path) {
                        if(path.equals("/teste")){
                            List<ACL> auth = new ArrayList(Collections.singletonList(new ACL(ZooDefs.Perms.ALL, new Id("auth", "admin:admin"))));
                            return auth;
                        }else if (path.equals("/test21")) {
                            return Collections.singletonList(new ACL(ZooDefs.Perms.ALL, new Id("auth", "admin1:admin")));
                        }
                        return ZooDefs.Ids.OPEN_ACL_UNSAFE;
                    }
                })*/
                //这个创建目录的时候是不可以的只适合于读
                .authorization(Arrays.asList(new AuthInfo("digest","zty:1".getBytes())))
                .connectString(ip+":"+zkPort)
                .sessionTimeoutMs(25000)
                .connectionTimeoutMs(25000)
                .retryPolicy(retryPolicy)
                //.authorization(Collections.singletonList(new AuthInfo("digest", "admin:admin".getBytes())))
                .build();
             client.start();
    }


    public static boolean createPath(String path) throws Exception {
        client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                //.withACL(Collections.singletonList(new ACL(ZooDefs.Perms.ALL, new Id("auth", "admin:admin"))))
                .forPath(path, "hello".getBytes());
        Stat stat = client.checkExists().forPath(path);
        return stat != null;
    }

    /**
     * 查询路径
     * @param path
     * @throws Exception
     */
    public static ZkEntity  getPath(String path) throws Exception {
        //查询stat
        Stat stat = new Stat();
        byte[] data = client.getData().storingStatIn(stat).forPath(path);
        ZkEntity zkEntity = new ZkEntity();
        zkEntity.setData(data);
        zkEntity.setStat(stat);
        //查询子节点
        List<String> childNodes = client.getChildren().forPath(path);
        zkEntity.setChildNodePaths(childNodes);
        return zkEntity;
    }


    public static void main(String[] agrs) throws Exception {
        getPath("/ztytest1");
    }


}
