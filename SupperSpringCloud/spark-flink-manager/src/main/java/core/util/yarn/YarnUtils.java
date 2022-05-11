package core.util.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class YarnUtils {
    public static YarnClient yarnClient;
   static {
        Configuration configuration = new YarnConfiguration();
        configuration.addResource(new Path("core-site.xml"));
        configuration.addResource(new Path("hdfs-site.xml"));
        configuration.addResource(new Path("yarn-site.xml"));
        yarnClient = YarnClient.createYarnClient();
        yarnClient.init(configuration);
        yarnClient.start();
    }
    public static YarnApplicationState getYarnState(ApplicationId applicationId) throws IOException, YarnException {
        ApplicationReport applicationReport = yarnClient.getApplicationReport(applicationId);
        return applicationReport.getYarnApplicationState();
    }

    public static void getApplicationIdByName(String... yarnAppName) throws IOException, YarnException {
        Set<String> appName = new HashSet<String>();
        List<ApplicationReport> applications = yarnClient.getApplications(appName);

    }


}
