package com.networknt.chaos;

public class ChaosMonkeyConfig {
    final public static String CONFIG_NAME = "chaos-monkey";
    boolean enabled;
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

