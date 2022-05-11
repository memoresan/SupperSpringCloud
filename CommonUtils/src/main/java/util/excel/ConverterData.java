package util.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.format.NumberFormat;

public class ConverterData {
    /**
     * converter属性定义自己的字符串转换器
     */
    @ExcelProperty(converter = CustomStringStringConverter.class)
    private String string;
    /**
     * 这里用string 去接日期才能格式化
     */
    @DateTimeFormat("yyyy年MM月dd日 HH时mm分ss秒")
    private String date;
    /**
     * 我想接收百分比的数字
     */
    @NumberFormat("#.##%")
    private String doubleData;


    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDoubleData() {
        return doubleData;
    }

    public void setDoubleData(String doubleData) {
        this.doubleData = doubleData;
    }

    @Override
    public String toString() {
        return "ConverterData{" +
                "string='" + string + '\'' +
                ", date='" + date + '\'' +
                ", doubleData='" + doubleData + '\'' +
                '}';
    }
}
