package util.jdk;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {
    //private static Logger logger = LoggerUtil.getLogger();

    public static void main(String[] args) {
       /*
       //只有年月
        LocalDate localDate = LocalDate.now();
        //System.out.println("localDate: " );
        //时分秒.毫秒
        LocalTime localTime = LocalTime.now();
        //System.out.println("localTime: " + localTime);
        //根据上面两个对象，获取日期时间
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        System.out.println("localDateTime: " + locateDateTimeToString("yyyy-MM-dd HH:mm:ss",localDateTime));
        //使用静态方法生成此对象
        LocalDateTime localDateTime2 = LocalDateTime.now();
        System.out.println("localDateTime2: " + localDateTime2);
        //格式化时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
        System.out.println("格式化之后的时间: " + localDateTime2.format(formatter));
        Date date = new Date();
        System.out.println(date);
        System.out.println(dateToLocalDate(date));*/
        System.out.println(getDateTimeByMillli(1638339830754L));
    }

    public DateUtils() {
    }

    /**
     * localDateTime转成毫秒
     * @param localDateTime
     * @return
     */
    public static long dateTimeToTimeStamp(LocalDateTime localDateTime){
        return localDateTime.toEpochSecond(ZoneOffset.of("+8"));
    }

    /**
     * 毫秒数转成 localDateTome
     * @param epochMilli
     * @return
     */
    public static LocalDateTime getDateTimeByMillli(long epochMilli){
        //时间戳(毫秒)转化成LocalDateTime
        Instant instant = Instant.ofEpochMilli(epochMilli);
        return LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
    }

    /**
     * 秒数转成LocalDataTime
     * @param epochSecond
     * @return
     */
    public static LocalDateTime getDateTimeBySecond(long epochSecond){
        //时间戳(秒)转化成LocalDateTime
        Instant instant2 = Instant.ofEpochSecond(epochSecond);
        return LocalDateTime.ofInstant(instant2, ZoneOffset.systemDefault());
    }

    /**
     * localDateTime转成string
     * @param format
     * @param localDateTime
     * @return
     */
    public static String locateDateTimeToString(String format, LocalDateTime localDateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(formatter);
    }

    /**
     * localTime转成string
     * @param format
     * @param localDateTime
     * @return
     */
    public static String locateDateToString(String format, LocalDate localDateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(formatter);
    }

    // Date 转化成 LocalDateTime
    public static LocalDateTime dateToLocalDate(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }




}

