package util.jdk;

import org.apache.commons.cli.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 记录string和string数组的转换
 */
public class StringUtils {


    /**
     * 首字母大寫
     * @param name
     * @return
     */
    public static String captureName(String name) {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return  name;

    }

    /**
     * resource下面的文件
     * @param path
     * @return
     */
    public static String getResourcePath(String path){
        return StringUtils.class.getClassLoader().getResource(path).getPath();
    }





    public static String join(List<String> s){
        return org.apache.commons.lang3.StringUtils.join(s,",");
    }

    public static void parseLine(String[] args) {
        try {
            // create Options object
            Options options = new Options();
            options.addOption(new CustomOption("t", "text", true, "use given information(String)",true));
            options.addOption(new Option("b", false, "display current time(boolean)"));
            options.addOption(new Option("s", "size", true, "use given size(Integer)"));
            options.addOption(new Option("f", "file", true, "use given file(File)"));
            options.addOption(new Option("h", "help", false, "help"));

            @SuppressWarnings("static-access")
            Option property = OptionBuilder.withArgName("property=value")
                    .hasArgs(2)
                    .withValueSeparator()
                    .withDescription("use value for given property(property=value)")
                    .create("D");
            property.setRequired(false);
            options.addOption(property);
            // create the command line parser
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);

            // check the options have been set correctly
            System.out.println(cmd.getOptionValue("t"));
            System.out.println(cmd.getOptionValue("f"));
            if (cmd.hasOption("h") || cmd.hasOption("--help")) {
                // print usage
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("AntOptsCommonsCLI", options);
            }
            System.out.println(cmd.getOptionValue("s"));
            System.out.println(cmd.getOptionProperties("D").getProperty("key1"));
            System.out.println(cmd.getOptionProperties("D").getProperty("key2"));
            System.out.println(cmd.getOptionProperties("D").getProperty("key3"));


        } catch (Exception ex) {
            System.out.println("Unexpected exception:" + ex.getMessage());
        }
    }

    public static void main(String[] agrs){
        /*String[] str = new String[]{"-h"};
        parseLine(str);*/
        String a = "hi";
        System.out.println(String.format("%s= :%s",a,a));

    }
}
