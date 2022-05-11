package util.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class HadoopUtils {
    public static Configuration configuration;
    //获取配置文件
    public static Configuration getHadoopConfiguration(){
        if(configuration == null){
            configuration = new Configuration();
            configuration.addResource(new Path("core-site.xml"));
            configuration.addResource(new Path("hdfs-site.xml"));
            configuration.addResource(new Path("yarn-site.xml"));
            configuration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        }
        return configuration;

    }

    public static FileSystem getFileSystem() throws IOException {
        FileSystem fileSystem = FileSystem.get(getHadoopConfiguration());
        return fileSystem;
    }


    public static void main(String[] args) throws IOException {
        System.out.println(HadoopUtils.getFileSystem().getFileStatus(new Path("/ztyTest")));
    }

}
