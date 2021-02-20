package com.networknt.chaos;

public class LatencyAssaultConfig {
    final public static String CONFIG_NAME = "latency-assault";
    boolean enabled;
    boolean bypass;
    int level;
    int latencyRangeStart;
    int latencyRangeEnd;

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

    public int getLatencyRangeStart() {
        return latencyRangeStart;
    }

    public void setLatencyRangeStart(int latencyRangeStart) {
        this.latencyRangeStart = latencyRangeStart;
    }

    public int getLatencyRangeEnd() {
        return latencyRangeEnd;
    }

    public void setLatencyRangeEnd(int latencyRangeEnd) {
        this.latencyRangeEnd = latencyRangeEnd;
    }
}
