package core.util.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class HadoopUtils {
    private static Configuration configuration;
    public static Configuration getConfiguration() {
        if (configuration == null) {
            synchronized (HadoopUtils.class) {
                if (configuration == null) {
                    configuration = new Configuration();
                    configuration.addResource(new Path("core-site.xml"));
                    configuration.addResource(new Path("hdfs-site.xml"));
                    configuration.addResource(new Path("yarn-site.xml"));
                    configuration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
                }
            }
        }
        return configuration;
    }
    public static FileSystem getFsSystem() throws IOException {
        FileSystem fileSystem = FileSystem.get(getConfiguration());
        return fileSystem;
    }
}
