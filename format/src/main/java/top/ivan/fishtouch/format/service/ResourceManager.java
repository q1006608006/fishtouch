package top.ivan.fishtouch.format.service;

import com.google.common.base.Strings;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import top.ivan.fishtouch.format.Constant;
import top.ivan.fishtouch.format.bean.ProfileConfig;
import top.ivan.fishtouch.format.utils.FileUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Ivan
 * @description
 * @date 2020/4/26
 */
public class ResourceManager {

    public static final int TAB_BLANK = 4;

    public static final String PROFILE_ENV = "@profile.env@";

    public static final String SCRIPT_MAIN_CLASS = "@script.mainClass@";

    public static final String SCRIPT_MAIN_JAR = "@script.mainJar@";

    public static final String ASSEMBLY_EXT_LIBS = "@assembly.extLibs@";

    public static final String ASSEMBLY_PROFILE = "@assembly.profiles@";

    private static final HashMap<String, String> exampleMap = new HashMap<>();

    private String mainClass;

    private ProfileConfig environment;

    private MavenProject project;

    private List<String> extLibs;

    public ResourceManager(String mainClass, ProfileConfig environment, MavenProject project, List<String> extLibs) {
        this.mainClass = mainClass;
        this.environment = environment;
        this.project = project;
        this.extLibs = extLibs;
    }

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

    public String getAssembly() throws IOException, URISyntaxException {
        String exampleSrc = loadExample(Constant.ASSEMBLY_EXAMPLE);

        //获取关联路径
        List<String> paths = new ArrayList<>();
        if (null != environment.getRelative()) {
            paths.addAll(environment.getRelative());
        }
        if (StringUtils.isNotBlank(environment.getLocation())) {
            paths.add(environment.getLocation());
        }

        List<String> fileSets = new ArrayList<>();

        for (String relative : paths) {
            AssemblyFileSet fs = new AssemblyFileSet();
            fs.setDirectory(Paths.get(relative, "${profile.env}").toString().replace("\\", "/"));
            fs.setOutputDirectory("conf");
            fs.setFiltered(false);
            fs.setExcludes(Collections.singletonList(environment.getProfileName()));
            fileSets.add(fs.toString());
        }

        String assemblyProfilesText = String.join("", fileSets);
        fileSets.clear();

        if (null != this.extLibs) {
            for (String extLib : this.extLibs) {
                AssemblyFileSet fs = new AssemblyFileSet();
                fs.setDirectory(extLib);
                fs.setOutputDirectory("libs");
                fs.setFiltered(false);
                fileSets.add(fs.toString());
            }
        }

        String assemblyExtLibsText = String.join("", fileSets);

        return exampleSrc.replace(ASSEMBLY_EXT_LIBS, assemblyExtLibsText).replace(ASSEMBLY_PROFILE, assemblyProfilesText);
    }

    public String getPom() throws IOException, URISyntaxException {
        String exampleSrc = loadExample(Constant.POM_EXAMPLE);

        //todo 替换@pom.extResource@

        //todo 替换@pom.filters@ 3个tab

        //todo 替换@pom.filters@ 4个tab

        //todo 替换@pom.assembly.descriptor@

        //todo 替换@pom.profiles@
    }

    private static String loadExample(String name) throws IOException, URISyntaxException {
        if (!exampleMap.containsKey(name)) {
            String src = FileUtil.loadResourceAsString(name);
            exampleMap.put(name, src);
        }
        return exampleMap.get(name);
    }

    private static String getTabStr(String src, int tabCount) {
        int blankCount = tabCount * TAB_BLANK;
        return Strings.repeat(" ", blankCount) + src;
    }

    private static String getXmlElement(String tag, Object val) {
        return "<" + tag + ">" + val + "</" + tag + ">";
    }


    private static class AssemblyFileSet {
        public static final int rootTab = 2;
        public static final int secTab = rootTab + 1;
        public static final int trdTab = secTab + 1;

        private String directory;
        private String outputDirectory;
        private String fileMode;
        private boolean filtered = false;
        private List<String> excludes;
        private List<String> includes;
        private String lineEnding;

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getOutputDirectory() {
            return outputDirectory;
        }

        public void setOutputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        public String getFileMode() {
            return fileMode;
        }

        public void setFileMode(String fileMode) {
            this.fileMode = fileMode;
        }

        public boolean isFiltered() {
            return filtered;
        }

        public void setFiltered(boolean filtered) {
            this.filtered = filtered;
        }

        public List<String> getExcludes() {
            return excludes;
        }

        public void setExcludes(List<String> excludes) {
            this.excludes = excludes;
        }

        public List<String> getIncludes() {
            return includes;
        }

        public void setIncludes(List<String> includes) {
            this.includes = includes;
        }

        public String getLineEnding() {
            return lineEnding;
        }

        public void setLineEnding(String lineEnding) {
            this.lineEnding = lineEnding;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(getTabStr("<fileSet>", rootTab)).append('\n');
            if (StringUtils.isNotBlank(directory)) {
                builder.append(getTabStr(getXmlElement("directory", directory), secTab)).append('\n');
            }
            if (StringUtils.isNotBlank(outputDirectory)) {
                builder.append(getTabStr(getXmlElement("outputDirectory", outputDirectory), secTab)).append('\n');
            }
            builder.append(getTabStr(getXmlElement("filtered", filtered), secTab)).append('\n');
            if (StringUtils.isNotBlank(fileMode)) {
                builder.append(getTabStr(getXmlElement("fileMode", fileMode), secTab)).append('\n');
            }
            if (StringUtils.isNotBlank(lineEnding)) {
                builder.append(getTabStr(getXmlElement("lineEnding", lineEnding), secTab)).append('\n');
            }
            if (null != excludes && excludes.size() > 0) {
                builder.append(getTabStr("<excludes>", secTab)).append('\n');
                for (String exclude : excludes) {
                    builder.append(getTabStr(getXmlElement("exclude", exclude), trdTab)).append('\n');
                }
                builder.append(getTabStr("</excludes>", secTab)).append('\n');
            }
            if (null != includes && includes.size() > 0) {
                builder.append(getTabStr("<includes>", secTab)).append('\n');
                for (String include : includes) {
                    builder.append(getTabStr(getXmlElement("include", include), trdTab)).append('\n');
                }
                builder.append(getTabStr("</includes>", secTab)).append('\n');
            }
            builder.append(getTabStr("</fileSet>", rootTab)).append('\n');

            return builder.toString();
        }

    }

    private static class PomResource {

        public static final int rootTab = 3;
        public static final int secTab = rootTab + 1;
        public static final int trdTab = secTab + 1;

        private String directory;
        private boolean filtering;

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public boolean isFiltering() {
            return filtering;
        }

        public void setFiltering(boolean filtering) {
            this.filtering = filtering;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(getTabStr("<resource>", rootTab)).append('\n');

            builder.append(getTabStr(getXmlElement("filtering", filtering), secTab)).append('\n');
            builder.append(getTabStr(getXmlElement("directory", directory), secTab)).append('\n');

            builder.append(getTabStr("</resource>", rootTab)).append('\n');
            return builder.toString();
        }
    }

}
