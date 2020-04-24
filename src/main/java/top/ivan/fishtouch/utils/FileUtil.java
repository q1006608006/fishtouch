package top.ivan.fishtouch.utils;

import java.io.IOException;
import java.io.InputStream;
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

}
