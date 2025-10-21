package com.networknt.chaos;

import com.networknt.config.Config;
import com.networknt.config.schema.ConfigSchema;
import com.networknt.config.schema.OutputFormat;
import com.networknt.config.schema.BooleanField;
import com.networknt.config.schema.IntegerField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@ConfigSchema(
        configKey = "killapp-assault",
        configName = "killapp-assault",
        configDescription = "Light Chaos Monkey Kill App Assault Handler Configuration.",
        outputFormats = {OutputFormat.JSON_SCHEMA, OutputFormat.YAML}
)
public class KillappAssaultConfig {
    private static final Logger logger = LoggerFactory.getLogger(KillappAssaultConfig.class);

    public final static String CONFIG_NAME = "killapp-assault";
    public final static String ENABLED = "enabled";
    public final static String BYPASS = "bypass";
    public final static String LEVEL = "level";

    private Map<String, Object> mappedConfig;
    private final Config config;

    @BooleanField(
            configFieldName = ENABLED,
            externalizedKeyName = ENABLED,
            description = "Enable the handler if set to true so that it will be wired in the handler chain during the startup.",
            externalized = true,
            defaultValue = "false"
    )
    boolean enabled;

    @BooleanField(
            configFieldName = BYPASS,
            externalizedKeyName = BYPASS,
            description = "Bypass the current chaos monkey middleware handler so that attacks won't be triggered.",
            externalized = true,
            defaultValue = "true"
    )
    boolean bypass;

    @IntegerField(
            configFieldName = LEVEL,
            externalizedKeyName = LEVEL,
            description = "How many requests are to be attacked. 1 each request, 5 each 5th request is attacked.",
            externalized = true,
            defaultValue = "10"
    )
    int level;

    // --- Constructor and Loading Logic ---

    private KillappAssaultConfig() {
        this(CONFIG_NAME);
    }

    private KillappAssaultConfig(String configName) {
        config = Config.getInstance();
        mappedConfig = config.getJsonMapConfigNoCache(configName);
        setConfigData();
    }

    public static KillappAssaultConfig load() {
        return new KillappAssaultConfig();
    }

    public static KillappAssaultConfig load(String configName) {
        return new KillappAssaultConfig(configName);
    }

    public void reload() {
        mappedConfig.clear();
        mappedConfig.putAll(config.getJsonMapConfigNoCache(CONFIG_NAME));
        setConfigData();
    }

    private void setConfigData() {
        Object object = mappedConfig.get(ENABLED);
        if(object != null) enabled = Config.loadBooleanValue(ENABLED, object);

        object = mappedConfig.get(BYPASS);
        if(object != null) bypass = Config.loadBooleanValue(BYPASS, object);

        object = mappedConfig.get(LEVEL);
        if(object != null) level = Config.loadIntegerValue(LEVEL, object);
    }

    // --- Getters and Setters (Original Methods) ---

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