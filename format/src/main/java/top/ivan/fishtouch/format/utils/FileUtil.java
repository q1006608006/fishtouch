package top.ivan.fishtouch.format.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        URL fileURL = FileUtil.class.getClassLoader().getResource(name);
        if (null == fileURL) {
            throw new FileNotFoundException(String.format("未找到资源: 'classes/%s'", name));
        }
        List<String> lines = Files.readAllLines(Paths.get(fileURL.toURI()), Charset.defaultCharset());
        return String.join("\n", lines);
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

}
