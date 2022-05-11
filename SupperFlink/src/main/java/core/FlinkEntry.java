package core;


import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.runtime.execution.librarycache.FlinkUserCodeClassLoaders;
import org.apache.flink.util.FlinkUserCodeClassLoader;
import util.YmlUtils;
import util.jdk.FileUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static util.jdk.DateUtils.getDateTimeByMillli;

public class FlinkEntry {


    public static void main(String[] agrs) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        String path = FlinkEntry.class.getProtectionDomain().getCodeSource().getLocation().getPath();
       /* URLClassLoader contextClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        Method addMethod = contextClassLoader.getClass().getSuperclass().getDeclaredMethod("addURL", URL.class);
        addMethod.setAccessible(true);
        addMethod.invoke(contextClassLoader, new File("E:\\program\\BigDataProgram\\CommonUtils\\target\\CommonUtils-1.0-SNAPSHOT.jar").getAbsoluteFile().toURL()
        );*/
        File file = new File(path);
        File[] files = file.getParentFile().listFiles(
                x -> x.isFile() //&& !x.getName().equals(path)
                        && x.getName().toLowerCase().endsWith(".jar"));
        if (files != null) {
            URL[] all = new URL[files.length];
            for (int i = 0; i < files.length; i++) {
                all[i] = files[i].toURI().toURL();
                // System.out.println( files[i].toURI().toURL().getPath());
            }
            //List<URL> commonUtils = Arrays.stream(all).filter(x -> x.getPath().contains("CommonUtils")).collect(Collectors.toList());
            System.out.println(Arrays.stream(all).filter(x -> x.getPath().contains("CommonUtils")).count());


            URLClassLoader urlClassLoader1 = new URLClassLoader(all, null);
            String[] alwaysParentFirstLoaderPatterns = "".split(";");
            System.out.println("----"+FlinkEntry.class.getClassLoader().getParent());
            System.out.println("----"+FlinkEntry.class.getClassLoader());
            System.out.println("----"+Thread.currentThread().getContextClassLoader());
            System.out.println("----"+Thread.currentThread().getContextClassLoader().getParent());
            Thread.currentThread().setContextClassLoader(
                    FlinkUserCodeClassLoaders.childFirst(all, urlClassLoader1, alwaysParentFirstLoaderPatterns, (x) -> {
                        x.printStackTrace();
                    }, false));
            /*ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
            if (clsLoader instanceof URLClassLoader)
            {
                Method addMethod = clsLoader.getClass().getSuperclass().getDeclaredMethod("addURL",
                        URL.class);
                addMethod.setAccessible(true);
                List<Object> listArr = new ArrayList<>();
                URL url = new URL("file://" + "/home/admin/ztyTest/lib/CommonUtils-1.0-SNAPSHOT.jar");
                listArr.add(url);
                addMethod.invoke(clsLoader, listArr.toArray());
                System.out.println("----结束");
            }*/
            //URLClassLoader urlClassLoader = new URLClassLoader(all,contextClassLoader);
           // System.out.println(Arrays.stream(()clsLoader.getURLs()).filter(x -> x.getPath().contains("CommonUtils")).count());
            System.out.println( Thread.currentThread().getContextClassLoader().getResource("util/jdk"));
            //System.out.println(urlClassLoader1.getResource("util/jdk"));
            // 这里必须通过反射的方式来启动，不能直接依赖启动
            //Class<?> cls = contextClassLoader.loadClass("core.FlinkStart");
            //Thread.currentThread().setContextClassLoader(urlClassLoader);

            Class<?> cls = Class.forName("core.FlinkStart", true,
                    urlClassLoader1);
            Method mainMethod = cls.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) agrs);
        }
    }
}


