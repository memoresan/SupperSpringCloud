package core;

import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;

public class StartApp {
    public void startApp(){
        SparkLauncher sparkLauncher = new SparkLauncher();
        /*sparkLauncher.setDeployMode("yarn-cluster");
        //sparkLauncher.redirectOutput()
        sparkLauncher.startApplication(new SparkAppHandle.Listener() {
            @Override
            public void stateChanged(SparkAppHandle handle) {


            }

            @Override
            public void infoChanged(SparkAppHandle handle) {

            }

        });*/
    }


}
