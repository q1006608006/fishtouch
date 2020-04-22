package top.ivan.fishtouch.bean;

import java.util.Collections;
import java.util.List;

/**
 * @author Ivan
 * @description
 * @date 2020/4/21
 */
public class AssemblyConfig {

    private List<String> extLibs = Collections.singletonList("libs");

    private String file = "assembly.xml";

    private String path = "assembly";

    public List<String> getExtLibs() {
        return extLibs;
    }

    public void setExtLibs(List<String> extLibs) {
        this.extLibs = extLibs;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
