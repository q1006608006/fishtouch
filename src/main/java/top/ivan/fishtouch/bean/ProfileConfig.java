package top.ivan.fishtouch.bean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ivan
 * @description
 * @date 2020/4/20
 */
public class ProfileConfig {

    private List<String> relative;

    private Set<String> envs = new HashSet<>(Arrays.asList("dev", "test", "prod"));

    private String location = "src/main/profile";

    private String profileName = "profile.properties";

    public List<String> getRelative() {
        return relative;
    }

    public void setRelative(List<String> relative) {
        this.relative = relative;
    }

    public Set<String> getEnvs() {
        return envs;
    }

    public void setEnvs(Set<String> envs) {
        this.envs = envs;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
}
