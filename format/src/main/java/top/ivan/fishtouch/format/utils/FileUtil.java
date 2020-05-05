package top.ivan.fishtouch.format.utils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Ivan
 * @description
 * @date 2020/4/24
 */
public class FileUtil {

    public static void createDirIfEmpty(Path path) throws IOException {
        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }

    }

    public static InputStream loadResource(String name) {
        return FileUtil.class.getClassLoader().getResourceAsStream(name);
    }

    public static String loadResourceAsString(String name) throws URISyntaxException, IOException {
        InputStream stream = FileUtil.class.getClassLoader().getResourceAsStream(name);
        if (null == stream) {
            throw new FileNotFoundException(String.format("未找到资源: 'classes/%s'", name));
        }
        BufferedInputStream in = new BufferedInputStream(stream);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int t;
        while ((t = in.read()) > 0) {
            out.write(t);
        }

        out.close();
        in.close();

        return out.toString(Charset.defaultCharset().toString());
    }

    public static void writeToFile(Path path, String source) throws IOException {

        RandomAccessFile toFile = new RandomAccessFile(path.toFile(), "rw");
        long curPos = toFile.length();
        toFile.seek(curPos);

        toFile.write(source.getBytes(Charset.defaultCharset()));
        toFile.close();
    }

    public static void writeIfNotExists(Path path, String source) throws IOException {
        if (!Files.exists(path)) {
            writeToFile(path, source);
        }
    }

    public static void overrideFile(Path path,String source) throws IOException {
            Files.deleteIfExists(path);
            writeToFile(path,source);
    }

}
