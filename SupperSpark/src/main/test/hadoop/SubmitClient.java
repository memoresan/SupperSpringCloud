package hadoop;

import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.client.api.impl.YarnClientImpl;
import org.apache.hadoop.yarn.exceptions.YarnException;
import util.hadoop.HadoopUtils;

import java.io.IOException;

public class SubmitClient {

    public static void main(String[] args) throws IOException, YarnException {
        YarnClient yarnClient = new YarnClientImpl();
        yarnClient.init(HadoopUtils.getHadoopConfiguration());
        yarnClient.start();
        YarnClientApplication application = yarnClient.createApplication();
        GetNewApplicationResponse newAppResponse = application.getNewApplicationResponse();
        ApplicationId applicationId = newAppResponse.getApplicationId();
        applicationId.getId();


    }





}
