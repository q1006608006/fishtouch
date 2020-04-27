package top.ivan.fishtouch.format;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import top.ivan.fishtouch.format.bean.ProfileConfig;
import top.ivan.fishtouch.format.service.ResourceManager;
import top.ivan.fishtouch.format.utils.FileUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan
 * @description
 * @date 2020/4/20
 */
@Mojo(name = "format")
public class FormatMojo extends AbstractMojo {

    @Parameter(defaultValue = "true")
    private boolean createExample;

    @Parameter
    private ProfileConfig environment = new ProfileConfig();

    @Parameter(defaultValue = "src/main/libs")
    private List<String> extLibs;

    @Parameter(defaultValue = "springboot")
    private String shellType;

    @Parameter(defaultValue = "${mainClass}")
    private String mainClass;

    @Parameter(
            defaultValue = "${project}",
            readonly = true,
            required = true
    )
    private MavenProject project;

    private ResourceManager resourceManager;

    private void init() {
        resourceManager = new ResourceManager(mainClass, environment, project, extLibs);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //初始化
        init();

        //安装环境文件夹及对应文件
        if (environment != null) {
            try {
                installEnvs();
            } catch (Exception e) {
                throw new MojoExecutionException("安装环境文件时发生错误", e);
            }
        }

        //安装脚本、assembly资源、libs文件夹
        try {
            installResources();
        } catch (Exception e) {
            throw new MojoExecutionException("安装资源文件时发生错误", e);
        }
        //todo: 创建example文件
        if (createExample) {
            try {
                installExample();
            } catch (Exception e) {
                throw new MojoExecutionException("安装模板文件时发生错误", e);
            }
        }

    }

    private void installResources() throws IOException, URISyntaxException {

        //创建外部jar包文件夹
        if (extLibs.size() > 0) {
            for (String lib : extLibs) {
                FileUtil.createDirIfEmpty(Paths.get(lib));
            }
        }

        //安装脚本
        //创建文件夹
        FileUtil.createDirIfEmpty(Paths.get(Constant.SCRIPTS_PATH));
        //安装脚本文件
        FileUtil.writeIfNotExists(Paths.get(Constant.SCRIPTS_RUN_PATH), resourceManager.getRun(shellType));
        FileUtil.writeIfNotExists(Paths.get(Constant.SCRIPTS_STOP_PATH), resourceManager.getStop(shellType));
        FileUtil.writeIfNotExists(Paths.get(Constant.SCRIPTS_BAT_PATH), resourceManager.getBat(shellType));


        //安装assembly文件夹及assembly.xml文件
        FileUtil.createDirIfEmpty(Paths.get(Constant.ASSEMBLY_PATH));
        FileUtil.writeIfNotExists(Paths.get(Constant.ASSEMBLY_FILE_PATH), resourceManager.getAssembly());

    }

    private void installExample() {

    }

    private void installEnvs() throws IOException, URISyntaxException {

        List<String> dirs = new ArrayList<>();
        //获取文件夹列表，判断文件夹是否存在，不存在则创建
        //获取相对路径（一般用于全局）
        if (environment.getRelative() != null) {
            dirs.addAll(environment.getRelative());
        }
        //获取本地路径
        if (null != environment.getLocation() && !"false".equals(environment.getLocation())) {
            dirs.add(environment.getLocation());
        }

        //遍历文件路径，若资源不完整则创建对应资源文件
        for (String currDir : dirs) {
            //创建profile文件夹
            Path baseDirPath = Paths.get(currDir);
            FileUtil.createDirIfEmpty(baseDirPath);

            //创建env文件夹
            for (String env : environment.getEnvs()) {
                Path envDirPath = Paths.get(baseDirPath.toString(), env);
                FileUtil.createDirIfEmpty(envDirPath);

                String profileName = environment.getProfileName();
                Path profilePath = Paths.get(envDirPath.toString(), profileName);
                FileUtil.writeIfNotExists(profilePath, resourceManager.getProfile(env));
            }
        }

    }

}
