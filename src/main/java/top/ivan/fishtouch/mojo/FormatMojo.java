package top.ivan.fishtouch.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import top.ivan.fishtouch.bean.AssemblyConfig;
import top.ivan.fishtouch.bean.ProfileConfig;

import java.util.Set;

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

    @Parameter
    private AssemblyConfig assembly;

    @Parameter(
            defaultValue = "${project.build.finalName}",
            readonly = true
    )
    private String finalName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        //todo: 安装环境

        //todo: 安装脚本、assembly资源、libs文件夹、deploy文件

        //todo: 创建example文件

    }

}
