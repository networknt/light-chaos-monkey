package com.networknt.chaos;

public class KillappAssaultConfig {
    final public static String CONFIG_NAME = "killapp-assault";
    boolean enabled;
    boolean bypass;
    int level;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isBypass() {
        return bypass;
    }

    public void setBypass(boolean bypass) {
        this.bypass = bypass;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
