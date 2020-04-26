package top.ivan.fishtouch.format.service;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import top.ivan.fishtouch.format.Constant;
import top.ivan.fishtouch.format.utils.FileUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * @author Ivan
 * @description
 * @date 2020/4/26
 */
public class ResourceManager {

    public static final String PROFILE_ENV = "@profile.env@";

    public static final String SCRIPT_MAIN_CLASS = "@script.mainClass@";

    public static final String SCRIPT_MAIN_JAR = "@script.mainJar@";

    private static final HashMap<String, String> exampleMap = new HashMap<>();

    @Parameter(defaultValue = "${mainClass}")
    private String mainClass;

    @Parameter(
            defaultValue = "${project}",
            readonly = true,
            required = true
    )
    private MavenProject project;

    public String getProfile(String env) throws IOException, URISyntaxException {
        String src = loadExample(Constant.PROFILE_EXAMPLE);
        return src.replace(PROFILE_ENV, env);
    }

    public String getRun(String type) throws IOException, URISyntaxException {
        if ("springboot".equals(type)) {
            return loadExample(Constant.SCRIPT_RUN_SPRINGBOOT_EXAMPLE).replace(SCRIPT_MAIN_JAR, project.getBuild().getFinalName());
        }

        return loadExample(Constant.SCRIPT_RUN_COMMON_EXAMPLE).replace(SCRIPT_MAIN_CLASS, mainClass);
    }

    public String getStop(String type) throws IOException, URISyntaxException {
        if ("springboot".equals(type)) {
            return loadExample(Constant.SCRIPT_STOP_SPRINGBOOT_EXAMPLE);
        }

        return loadExample(Constant.SCRIPT_STOP_COMMON_EXAMPLE);
    }

    public String getBat(String type) throws IOException, URISyntaxException {
        if ("springboot".equals(type)) {
            return loadExample(Constant.SCRIPT_BAT_SPRINGBOOT_EXAMPLE).replace(SCRIPT_MAIN_JAR, project.getBuild().getFinalName());
        }

        return loadExample(Constant.SCRIPT_BAT_COMMON_EXAMPLE).replace(SCRIPT_MAIN_CLASS, mainClass);
    }


    private static String loadExample(String name) throws IOException, URISyntaxException {
        if (!exampleMap.containsKey(name)) {
            String src = FileUtil.loadResourceAsString(name);
            exampleMap.put(name, src);
        }
        return exampleMap.get(name);
    }
}
