package util.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExcelListenerData extends AnalysisEventListener<LinkedHashMap<Integer,String>> {

    List<String> list = new ArrayList<>();

    @Override
    public void invoke(LinkedHashMap<Integer, String> data, AnalysisContext context) {
       list.add(JSONObject.toJSONString(data));
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        //context.currentReadHolder().readListenerList();
    }

    //读取表头内容
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        list.add(JSONObject.toJSONString(headMap));
        System.out.println("表头："+headMap);

    }

}
