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

    public String getBaseProfile() throws IOException, URISyntaxException {
        String src = loadExample(Constant.PROFILE_BASE_EXAMPLE);
        return src.replace(SCRIPT_MAIN_CLASS,mainClass);
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
        List<String> paths = getEnvBaseLocations();

        List<String> fileSets = new ArrayList<>();

        for (String relative : paths) {
            AssemblyFileSet fs = new AssemblyFileSet(2);
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
                AssemblyFileSet fs = new AssemblyFileSet(2);
                fs.setDirectory(extLib);
                fs.setOutputDirectory("libs");
                fs.setFiltered(false);
                fileSets.add(fs.toString());
            }
        }

        String assemblyExtLibsText = String.join("", fileSets);

        return exampleSrc.replace(ASSEMBLY_EXT_LIBS, assemblyExtLibsText).replace(ASSEMBLY_PROFILE, assemblyProfilesText);
    }


    /*
     * pom example file
     *
     */

    public static final String POM_EXT_RESOURCE = "@pom.extResource@";
    public static final String POM_BUILD_FILTERS = "@pom.build.filters@";
    public static final String POM_ASSEMBLY_FILTERS = "@pom.assembly.filters@";
    public static final String POM_ASSEMBLY_DESCRIPTOR = "@pom.assembly.descriptor@";
    public static final String POM_PROFILES = "@pom.profiles@";

    public String getPom() throws IOException, URISyntaxException {
        String exampleSrc = loadExample(Constant.POM_EXAMPLE);
        List<String> extResourceBodies = new ArrayList<>();
        List<String> filters = new ArrayList<>();

        //替换@pom.extResource@
        List<String> allLocation = getEnvBaseLocations();

        for (String location : allLocation) {
            PomResource pr = new PomResource();
            pr.setDirectory(location + "/${profile.env}");
            pr.setFiltering(false);
            pr.setExcludes(Collections.singletonList(environment.getProfileName()));
            extResourceBodies.add(pr.toString(3));

            filters.add(location +"/" + environment.getProfileName());
            filters.add(location + "/${profile.env}/" + environment.getProfileName());
        }
        exampleSrc = exampleSrc.replace(POM_EXT_RESOURCE, String.join("", extResourceBodies));
        extResourceBodies.clear();

        //替换@pom.build.filters@ 3tab
        StringBuilder bodyBuilder = new StringBuilder();
        for (String filter : filters) {
            bodyBuilder.append(getTabStr(getXmlElement("filter", filter), 3)).append("\n");
        }
        exampleSrc = exampleSrc.replace(POM_BUILD_FILTERS, bodyBuilder.toString());

        //替换@pom.assembly.filters@  6tab
        bodyBuilder = new StringBuilder();
        for (String filter : filters) {
            bodyBuilder.append(getTabStr(getXmlElement("filter", filter), 6)).append("\n");
        }
        exampleSrc = exampleSrc.replace(POM_ASSEMBLY_FILTERS, bodyBuilder.toString());

        //替换@pom.assembly.descriptor@
        exampleSrc = exampleSrc.replace(POM_ASSEMBLY_DESCRIPTOR, getTabStr(getXmlElement("descriptor", Constant.ASSEMBLY_FILE_PATH), 6));

        //替换@pom.profiles@
        List<String> profileBodies = new ArrayList<>();
        for (String env : environment.getEnvs()) {
            PomProfile ppf = new PomProfile(env, "dev".equals(env));
            profileBodies.add(ppf.toString(2));
        }

        return exampleSrc.replace(POM_PROFILES, String.join("", profileBodies));
    }

    private List<String> getEnvBaseLocations() {
        List<String> allLocation = new ArrayList<>();
        if (null != environment.getRelative()) {
            allLocation.addAll(environment.getRelative());
        }
        if (StringUtils.isNotBlank(environment.getLocation())) {
            allLocation.add(environment.getLocation());
        }
        return allLocation;
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

        private int rootTab = 2;

        private String directory;
        private String outputDirectory;
        private String fileMode;
        private boolean filtered = false;
        private List<String> excludes;
        private List<String> includes;
        private String lineEnding;

        public AssemblyFileSet() {
        }

        public AssemblyFileSet(int rootTab) {
            this.rootTab = rootTab;
        }

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
            return toString(rootTab);
        }

        public String toString(int tabCount) {
            int rootTab = tabCount;
            int secTab = rootTab + 1;
            int trdTab = secTab + 1;

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

        public int rootTab = 3;

        private String directory;
        private boolean filtering;
        private List<String> excludes;

        public PomResource() {
        }

        public PomResource(int rootTab) {
            this.rootTab = rootTab;
        }

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

        public List<String> getExcludes() {
            return excludes;
        }

        public void setExcludes(List<String> excludes) {
            this.excludes = excludes;
        }

        @Override
        public String toString() {
            return toString(rootTab);
        }

        public String toString(int tabCount) {
            int rootTab = tabCount;
            int secTab = rootTab + 1;
            int trdTab = secTab + 1;


            StringBuilder builder = new StringBuilder();
            builder.append(getTabStr("<resource>", rootTab)).append('\n');

            builder.append(getTabStr(getXmlElement("filtering", filtering), secTab)).append('\n');
            builder.append(getTabStr(getXmlElement("directory", directory), secTab)).append('\n');

            if (excludes != null) {
                builder.append(getTabStr("<excludes>", secTab)).append("\n");
                for (String exclude : excludes) {
                    builder.append(getTabStr(getXmlElement("exclude", exclude), trdTab)).append("\n");
                }
                builder.append(getTabStr("</excludes>", secTab)).append("\n");
            }

            builder.append(getTabStr("</resource>", rootTab)).append('\n');
            return builder.toString();
        }
    }

    private static class PomProfile {

        private String env;

        private boolean active;

        public PomProfile(String env, boolean active) {
            this.env = env;
            this.active = active;
        }

        public String toString() {
            return toString(2);
        }

        public String toString(int rootTab) {
            int secTab = rootTab + 1;
            int trdTab = secTab + 1;

            StringBuilder bd = new StringBuilder();

            bd.append(getTabStr("<profile>", rootTab)).append("\n");
            bd.append(getTabStr(getXmlElement("id", env), secTab)).append("\n");
            bd.append(getTabStr("<properties>", secTab)).append("\n");
            bd.append(getTabStr(getXmlElement("profile.env", env), trdTab)).append("\n");
            bd.append(getTabStr("</properties>", secTab)).append("\n");

            if (active) {
                bd.append(getTabStr("<activation>", secTab)).append("\n");
                bd.append(getTabStr(getXmlElement("activeByDefault", "true"), trdTab)).append("\n");
                bd.append(getTabStr("</activation>", secTab)).append("\n");
            }
            bd.append(getTabStr("</profile>", rootTab)).append("\n");

            return bd.toString();
        }

    }

}
