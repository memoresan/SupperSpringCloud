package util.cmd;

import java.io.*;
import java.util.*;

public class CmdUtils {
    /**
     * 环境变量
     */
    Map<String,String> environment ;
    /**
     * 工作文件
     */
    File directory;

    private Boolean hasContent = false;



    private NamedThreadFactory namedThreadFactory = new NamedThreadFactory("cmdUtils-proc-%d");

    public CmdUtils(boolean hasContent) {
        this.hasContent = hasContent;
    }

    public CmdUtils(Map<String, String> environment, File directory) {
        this.environment = environment;
        this.directory = directory;
    }

    public void executeCmd(StringBuffer content,String... cmd) {
        ProcessBuilder processBuilder=null;
        if(cmd.length == 1){
            StringTokenizer st = new StringTokenizer(cmd[0]);
            String[] cmdarray = new String[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++){
                cmdarray[i] = st.nextToken();
            }
            processBuilder= new ProcessBuilder(cmdarray);
        }else {
            processBuilder= new ProcessBuilder(cmd);
        }
       if(environment!=null ){
           processBuilder.environment().putAll(environment);
       }
       if(directory != null){
           processBuilder.directory(directory);
       }
       //当为true表示正常输出和标准输出放在一起
       processBuilder.redirectErrorStream(true);
       try {
           Process process = processBuilder.start();
           BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
           process(reader,content);
           int exitCode = process.waitFor();
           if(exitCode != 0){
               throw  new Exception("异常退出");
           }
       } catch (IOException e) {
           e.printStackTrace();
       } catch (InterruptedException e) {
           e.printStackTrace();
       } catch (Exception e) {
           e.printStackTrace();
       }

    }

    private void process(BufferedReader reader,StringBuffer content) {
        namedThreadFactory.newThread(()->{
            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                    content.append("\n");
                    System.out.println("output: " + line);
                }
            }catch(IOException e){
                e.printStackTrace();
            }finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }


    public static void main(String[] agrs){
        CmdUtils cmdUtils = new CmdUtils(false);
        String[] cmds = {"/bin/sh","-c","ps -ef|grep java"};
       //        cmdUtils.executeCmd(cmds);
    }

}
