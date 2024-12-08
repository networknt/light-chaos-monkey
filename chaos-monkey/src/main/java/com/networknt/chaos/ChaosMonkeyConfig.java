package com.networknt.chaos;

import com.networknt.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ChaosMonkeyConfig {
    private static final Logger logger = LoggerFactory.getLogger(ChaosMonkeyConfig.class);

    public final static String CONFIG_NAME = "chaos-monkey";
    public final static String ENABLED = "enabled";

    boolean enabled;
    final Config config;
    final Map<String, Object> mappedConfig;

    private ChaosMonkeyConfig() {
        this(CONFIG_NAME);
    }

    private ChaosMonkeyConfig(String configName) {
        config = Config.getInstance();
        mappedConfig = config.getJsonMapConfigNoCache(configName);
        setConfigData();
    }

    public static ChaosMonkeyConfig load() {
        return new ChaosMonkeyConfig();
    }

    public static ChaosMonkeyConfig load(String configName) {
        return new ChaosMonkeyConfig(configName);
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    void reload() {
        mappedConfig.clear();
        mappedConfig.putAll(config.getJsonMapConfigNoCache(CONFIG_NAME));
        setConfigData();
    }

    private void setConfigData() {
        Object object = mappedConfig.get(ENABLED);
        if(object != null) enabled = Config.loadBooleanValue(ENABLED, object);
    }

}
