package top.ivan.fishtouch.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import top.ivan.fishtouch.bean.AssemblyConfig;
import top.ivan.fishtouch.bean.ProfileConfig;

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
            defaultValue = "${project}",
            readonly = true,
            required = true
    )
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String finalName = project.getBuild().getFinalName();

        //todo: 安装环境

        //todo: 安装脚本、assembly资源、libs文件夹、deploy文件

        //todo: 创建example文件

    }

}
