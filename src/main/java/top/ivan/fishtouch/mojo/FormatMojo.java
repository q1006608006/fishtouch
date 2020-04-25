package top.ivan.fishtouch.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import top.ivan.fishtouch.Constant;
import top.ivan.fishtouch.bean.ProfileConfig;
import top.ivan.fishtouch.utils.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
    private ProfileConfig environment;

    @Parameter(defaultValue = "libs")
    private List<String> extLibs;

    @Parameter(defaultValue = "true")
    private boolean deploy;

    @Parameter(
            defaultValue = "${project}",
            readonly = true,
            required = true
    )
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String finalName = project.getBuild().getFinalName();

        //安装环境文件夹及对应文件
        if (environment != null) {
            try {
                installEnvs();
            } catch (Exception e) {
                throw new MojoExecutionException("安装环境文件时发生错误", e);
            }
        }

        //todo: 安装脚本、assembly资源、libs文件夹、deploy文件
        try {
            installResources();
        } catch (Exception e) {
            throw new MojoExecutionException("安装资源文件时发生错误",e);
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

    private void installResources() throws IOException {

        //创建外部jar包文件夹
        if (extLibs.size() > 0) {
            for (String lib : extLibs) {
                FileUtil.createDirIfEmpty(Paths.get(lib));
            }
        }

    }

    private void installExample() {

    }

    private void installEnvs() throws IOException {

        List<String> dirs = new ArrayList<>();
        //获取文件夹列表，判断文件夹是否存在，不存在则创建
        //获取相对路径（一般用于全局）
        if (environment.getRelative() != null) {
            dirs.addAll(environment.getRelative());
        }
        //获取本地路径
        if (null != environment.getLocation()) {
            dirs.add(environment.getLocation());
        }

        //获取环境列表
        Set<String> envs = environment.getEnvs();

        //加载模板文件
        InputStream profileExample = FileUtil.loadResource(Constant.PROFILE_EXAMPLE);
        profileExample.mark(0);


        //判断文件夹是否存在，若不存在则创建文件夹,同时判断该文件夹下相应文件
        for (String currDir : dirs) {
            //创建profile文件夹
            Path baseDirPath = Paths.get(currDir);
            FileUtil.createDirIfEmpty(baseDirPath);

            //创建env文件夹
            for (String env : envs) {
                Path envDirPath = Paths.get(baseDirPath.toString(), env);
                FileUtil.createDirIfEmpty(envDirPath);

                String profileName = environment.getProfileName();
                Path profilePath = Paths.get(envDirPath.toString(), profileName);

                //创建profile.properties文件
                if (Files.notExists(profilePath)) {
                    Files.copy(profileExample, profilePath);
                    profileExample.reset();
                }
            }
        }
        profileExample.close();

    }

    public static void main(String[] args) throws IOException {
        ProfileConfig conf = new ProfileConfig();

        conf.setEnvs(new HashSet<>(Arrays.asList("dev", "test")));

        conf.setRelative(Arrays.asList("profile"));

        FormatMojo mojo = new FormatMojo();

        mojo.environment = conf;

        mojo.installEnvs();
    }

}
