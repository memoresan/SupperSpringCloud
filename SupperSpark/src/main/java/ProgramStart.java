import org.apache.commons.cli.CommandLineParser;
import org.apache.hadoop.fs.Path;
import util.caffeine.CaffeineCacheUtil;
import util.hadoop.HadoopUtils;
import util.jdbc.JDBCUtils;

import java.io.IOException;
import java.util.Arrays;

public class ProgramStart {
    public static void main(String[] args) throws InterruptedException, IOException {
        //JDBCUtils.execute("create table test2 (a int,b string) partitioned by (pt string)");
        System.out.println(HadoopUtils.getFileSystem().getUri().getPath());

    }
}
