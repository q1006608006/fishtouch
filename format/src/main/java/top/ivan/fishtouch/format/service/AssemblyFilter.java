package top.ivan.fishtouch.format.service;

import org.apache.maven.plugins.annotations.Parameter;
import top.ivan.fishtouch.format.bean.ProfileConfig;

import java.util.List;

/**
 * @author Ivan
 * @description
 * @date 2020/4/26
 */
public class AssemblyFilter {

    @Parameter
    private ProfileConfig environment;

    @Parameter(defaultValue = "libs")
    private List<String> extLibs;




}
