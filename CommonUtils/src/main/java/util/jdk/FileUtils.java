package util.jdk;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

/**
 * fileUtils
 */
public class FileUtils{
    /**
     * file 全复制移动
     * @param src
     * @param target
     */
    public static void removeFile(String src,String target) throws IOException {
        Path srcPath = Paths.get(src);
        Path targetPath = Paths.get(target);
        Files.move(srcPath,targetPath);
    }

    /**
     * 从file 对象获取URL
     * @param file
     * @return
     * @throws MalformedURLException
     */
    public static URL getURLByFile(File file) throws MalformedURLException {
        return file.getAbsoluteFile().toURI().toURL();
    }

    /**
     * 检查是否是jar
     * @param jar
     * @throws IOException
     */
    public static void checkJarFile(URL jar) throws IOException {
        File jarFile;
        try {
            jarFile = new File(jar.toURI());
        } catch (URISyntaxException e) {
            throw new IOException("JAR file path is invalid '" + jar + '\'');
        }
        if (!jarFile.exists()) {
            throw new IOException("JAR file does not exist '" + jarFile.getAbsolutePath() + '\'');
        }
        if (!jarFile.canRead()) {
            throw new IOException("JAR file can't be read '" + jarFile.getAbsolutePath() + '\'');
        }

        try (JarFile ignored = new JarFile(jarFile)) {
            // verify that we can open the Jar file
        } catch (IOException e) {
            throw new IOException("Error while opening jar file '" + jarFile.getAbsolutePath() + '\'', e);
        }
    }


}
