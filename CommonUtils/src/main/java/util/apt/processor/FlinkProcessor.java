package util.apt.processor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import util.jdk.ClassUtils;
import util.jdk.StringUtils;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FlinkProcessor implements BaseProcessor {
    private TypeElement typeElement;
    private Filer filer;
    private Messager messager;
    private Configuration configuration;
    private Writer out = null;
    private String filePath = System.getProperty("user.dir");
    public FlinkProcessor(TypeElement typeElement, Filer filer,Messager messager){
        this.typeElement = typeElement;
        this.filer = filer;
        this.messager = messager;
        try {
            configuration = new Configuration(Configuration.VERSION_2_3_22);
            configuration.setDirectoryForTemplateLoading(new File(filePath+"\\src\\main\\resources\\flt"));
            configuration.setDefaultEncoding("UTF-8");
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void generateCode() throws Exception {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        Class<?> functionClass = ClassUtils.stringToClass(typeElement.getQualifiedName().toString(), null);
        String className = functionClass.getName().substring(functionClass.getName().lastIndexOf(".")+1);
        Method[] methods = functionClass.getDeclaredMethods();
        String classPath = filePath+"/src/main/java/" + functionClass.getName().replaceAll("\\.","/") +".java";
        for(Method method : methods) {
            //获取classpath
            dataMap.put("packageName", "util.function.flink");
            //import
            dataMap.put("methodPath", functionClass.getCanonicalName());
            //className
            dataMap.put("className", className+ StringUtils.captureName(method.getName()));
            //output
            dataMap.put("output",method.getReturnType().getTypeName());
            //input
            List<String> inputList = new ArrayList<>();
            for (Parameter parameter : method.getParameters()) {
                inputList.add(parameter.getType().getSimpleName()+" "+parameter.getName());
            }
            dataMap.put("input",StringUtils.join(inputList));
            dataMap.put("method",className+"."+method.getName());
            List<String> valueList = new ArrayList<>();
            for (Parameter parameter : method.getParameters()) {
                valueList.add(parameter.getName());
            }
            dataMap.put("value",StringUtils.join(valueList));
            Pattern p = Pattern.compile("^(import).*;$");
            List<String> str = Files.readAllLines(Paths.get(classPath));
            List<String> collectList = str.stream().filter(x ->{
                Matcher matcher = p.matcher(x);
                return matcher.find() && !x.contains("annotation");
            }).collect(Collectors.toList());
            dataMap.put("list",collectList);

            loadMisk(dataMap,className + StringUtils.captureName(method.getName()));
        }
    }

    public void loadMisk(Map<String, Object> dataMap,String className) throws IOException {
        String path ="\\flink.flt";
        Template template = configuration.getTemplate(path);
        String dir = filePath+"\\src\\main\\java\\util\\function\\flink";
        Files.createDirectories(Paths.get(dir));
        try ( OutputStream fos = new FileOutputStream( new File(dir, className+".java"))){
            Writer out = new OutputStreamWriter(fos);
            template.process(dataMap, out);
            out.flush();

           /* boolean b = ClassUtils.compiler(dir+"\\"+className+".java");
            if(b){
                FileUtils.removeFile(dir+"\\"+className+".class",FlinkProcessor.class.getClassLoader().getResource("\\util\\function").getPath());
            }else {
                throw new Exception("解析失败");
            }*/
        } catch (FileNotFoundException | TemplateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
