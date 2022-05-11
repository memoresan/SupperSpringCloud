package util.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ExcelUtils {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //实现excel读操作
        String filename = "E:\\program\\1.xlsx";
        //从0开始计数,是同一个线程，当第一个读完才读第二个
        //EasyExcel.read(filename,ConverterData.class,new ExcelListener()).sheet(0).headRowNumber(4).doRead();
        //EasyExcel.read(filename,new ExcelListenerData()).sheet(0).headRowNumber(4).doRead();
         ExcelListenerData data = new ExcelListenerData();
         EasyExcel.read(filename,data).sheet(0).headRowNumber(4).doRead();
         data.list.forEach(x->System.out.println(x));



    }



}
