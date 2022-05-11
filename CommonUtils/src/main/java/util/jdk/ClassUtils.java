package util.jdk;


import entity.java.BaseEntity;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;

import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * 该类里面含有所有的class反射相关的方法
 */
public class ClassUtils {
    private static final String CLASS_SUFFIX = ".class";
    private static final String CLASS_FILE_PREFIX = File.separator + "classes"  + File.separator;
    private static final String PACKAGE_SEPARATOR = ".";

    public static Class<?> stringToClass(String className,ClassLoader loader) throws ClassNotFoundException {
        if(loader == null){
            loader = Thread.currentThread().getContextClassLoader();
        }
        return Class.forName(className);
    }




    /** java 获取包下所有的类名 **/
    public static Set<String> getClazzName(String packageName, boolean showChildPackageFlag ) {
        Set<String> result = new HashSet<>();
        String suffixPath = packageName.replaceAll("\\.", "/");
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try{
            Enumeration<URL> urls = loader.getResources(suffixPath);
            while(urls.hasMoreElements()){
                URL url = urls.nextElement();
                if(url != null){
                    String protocol = url.getProtocol();
                    if("file".equals(protocol)){
                        String path = url.getPath();
                        result.addAll(getAllClassNameByFile(new File(path),showChildPackageFlag));
                    }else if("jar".equals(protocol)){
                        JarFile jarFile = null;
                        jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                        if(jarFile != null){
                            result.addAll(getAllClassNameByJar(jarFile,packageName,showChildPackageFlag));
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static List<String> getAllClassNameByFile(File file, boolean flag) {
        List<String> result =  new ArrayList<>();
        if(!file.exists()) {
            return result;
        }
        if(file.isFile()) {
            String path = file.getPath();
            if(path.endsWith(CLASS_SUFFIX)) {
                path = path.replace(CLASS_SUFFIX, "");
                String clazzName = path.substring(path.indexOf(CLASS_FILE_PREFIX) + CLASS_FILE_PREFIX.length())
                        .replace(File.separator, PACKAGE_SEPARATOR);
                if(-1 == clazzName.indexOf("$")) {
                    result.add(clazzName);
                }
            }
            return result;
        }else{
            File[] listFiles = file.listFiles();
            if(listFiles != null && listFiles.length > 0) {
                for (File f : listFiles) {
                    if(flag){
                        result.addAll(getAllClassNameByFile(f, flag));
                    }else{
                        if(f.isFile()){
                            String path = f.getPath();
                            if(path.endsWith(CLASS_SUFFIX)) {
                                path = path.replace(CLASS_SUFFIX, "");
                                String clazzName = path.substring(path.indexOf(CLASS_FILE_PREFIX) + CLASS_FILE_PREFIX.length())
                                        .replace(File.separator, PACKAGE_SEPARATOR);
                                if(-1 == clazzName.indexOf("$")) {
                                    result.add(clazzName);
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }
    }


    private static Set<String> getAllClassNameByJar(JarFile jarFile, String packageName, boolean flag) {
       Set<String> result =  new HashSet<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while(entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String name = jarEntry.getName();
            // 判断是不是class文件
            if(name.endsWith(CLASS_SUFFIX)) {
                name = name.replace(CLASS_SUFFIX, "").replace("/", ".");
                if(flag) {
                    // 如果要子包的文件,那么就只要开头相同且不是内部类就ok
                    if(name.startsWith(packageName) && -1 == name.indexOf("$")) {
                        result.add(name);
                    }
                } else {
                    // 如果不要子包的文件,那么就必须保证最后一个"."之前的字符串和包名一样且不是内部类
                    if(packageName.equals(name.substring(0, name.lastIndexOf("."))) && -1 == name.indexOf("$")) {
                        result.add(name);
                    }
                }
            }
        }
        return result;
    }


   public  static boolean  compiler(String path){
       JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
       List< String> optionList = new ArrayList<>();
       StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
       Iterable<? extends JavaFileObject> files =
               fileManager.getJavaFileObjectsFromStrings(
                       Arrays.asList(path));
       optionList.addAll((Arrays.asList("-classpath", System.getProperty("user.dir"))));
       return compiler.getTask(null,fileManager,null,optionList,null,files).call();
   }

    /**
     * 获取某一个列含有import的语句的内容，除了注解相关的包
     * @param cl 需要解析的class类
     * @return
     * @throws IOException
     */
   public static List<String> getImportName(Class<?> cl) throws IOException {
       String userDir = System.getProperty("user.dir");
       String classPath = userDir+"/src/main/java/" + cl.getName().replaceAll("\\.","/") +".java";
       List<String> str = Files.readAllLines(Paths.get(classPath));
       Pattern p = Pattern.compile("^(import).*;$");
       List<String> collectList = str.stream().filter(x ->{
           Matcher matcher = p.matcher(x);
           return matcher.find() && !x.contains("annotation");
       }).collect(Collectors.toList());
       return str;
   }




    /**
     * 这个方法多线程只能调用一次，获取某一个包下面所有属于baseEntity的子类
     * @param packagePath 包名称
     * @return 迭代器
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private static Iterator<Class<? extends BaseEntity>> getInstanceBySupperClass(String packagePath) throws IllegalAccessException, InstantiationException {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        //是哪个包
        Collection<URL> urls = ClasspathHelper.forPackage(packagePath);
        configurationBuilder.setUrls(urls);
        //根据url会获取缓存如果缓存文件关闭这句话会报错
        Reflections reflection = new Reflections(configurationBuilder);
        Set<Class<? extends BaseEntity>> subTypesOf = reflection.getSubTypesOf(BaseEntity.class);
        if(subTypesOf == null || subTypesOf.isEmpty()){
            return null;
        }else {
            return subTypesOf.iterator();
        }
    }


    public static byte[]  serializeJavaClass(Object t){
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectInputStream = new ObjectOutputStream(byteArrayOutputStream)){
            objectInputStream.writeObject(t);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object deSerializeJavaClass(byte[] bytes){
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)){
            objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            return null;
        }
    }






    public static void main(String[] agrs) throws InstantiationException, IllegalAccessException {

        Iterator<Class<? extends BaseEntity>> test = getInstanceBySupperClass("test");
        Iterator<Class<? extends BaseEntity>> test1 = getInstanceBySupperClass("test");

        while(test.hasNext()){
            //这个地方要单例
            BaseEntity baseEntity = test.next().newInstance();
            baseEntity.getUserName();

        }

    }
}
