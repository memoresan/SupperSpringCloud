package util.apt;;
import com.google.auto.service.AutoService;
import util.annotation.UdfAnnotation;
import util.apt.processor.BaseProcessor;
import util.apt.processor.FlinkProcessor;
import util.apt.processor.SparkProcessor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;

@SupportedAnnotationTypes(value ={"util.annotation.FunctionAnnotation","util.annotation.UdfAnnotation"})
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class UDFProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    private Elements elements;
    private List<BaseProcessor> processorList = new ArrayList<>();
    private Set<String> supportedAnnocationTypes = new HashSet<>();

    //初始化并且需要将其加入supportedAnnocationTypes
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        elements = processingEnv.getElementUtils();
        supportedAnnocationTypes.add(UdfAnnotation.class.getCanonicalName());

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnocationTypes;
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(UdfAnnotation.class);
        for(Element annotatedElement : elementsAnnotatedWith){
            //获取加在注解的实体类型，正常应该是class类
            if (annotatedElement.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                BaseProcessor processor = detailUdfType(typeElement);
                processorList.add(processor);
            }
        }
        for (BaseProcessor baseProcessor : processorList) {
            try {
                baseProcessor.generateCode();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * 处理udfType
     * @param typeElement
     */
    private BaseProcessor detailUdfType(TypeElement typeElement){
        UdfAnnotation annotation = typeElement.getAnnotation(UdfAnnotation.class);
        String udfType = annotation.udfType();
        if("flink".equals(udfType)){
            return new FlinkProcessor(typeElement,filer,messager);
        }else{
            return new SparkProcessor();
        }
    }
}
