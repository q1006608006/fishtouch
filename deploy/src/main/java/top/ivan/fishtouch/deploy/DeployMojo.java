package top.ivan.fishtouch.deploy;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Ivan
 * @description 快速部署
 * @date 2020/5/8
 */
@Mojo(name = "deploy-format")
public class DeployMojo extends AbstractMojo {

    @Parameter(
            defaultValue = "${project}",
            readonly = true,
            required = true
    )
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Properties prop;
        try {
            prop = loadProperties();
        } catch (IOException e) {
            throw new MojoExecutionException("加载配置文件失败", e);
        }
        for (String str : prop.stringPropertyNames()) {
            System.out.println(str + ": " + prop.get(str));
        }
    }

    private Properties loadProperties() throws IOException {
        Properties base = new Properties();
        for (String path : project.getFilters()) {
            Properties prop = new Properties();
            InputStream is = new FileInputStream(path);
            prop.load(is);
            is.close();
            for (String key : prop.stringPropertyNames()) {
                base.put(key, prop.get(key));
            }
        }

        Properties prop = project.getProperties();
        prop.putAll(base);
        return prop;
    }
}
