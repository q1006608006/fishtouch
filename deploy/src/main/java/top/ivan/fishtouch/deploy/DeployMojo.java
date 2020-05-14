package top.ivan.fishtouch.deploy;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author Ivan
 * @description 快速部署
 * @date 2020/5/8
 */
@Mojo(name = "deploy")
public class DeployMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }
}
