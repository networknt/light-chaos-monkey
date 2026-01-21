package com.networknt.chaos;

import com.networknt.config.Config;
import com.networknt.config.schema.ConfigSchema; // REQUIRED IMPORT
import com.networknt.config.schema.OutputFormat; // REQUIRED IMPORT
import com.networknt.config.schema.BooleanField; // REQUIRED IMPORT
import com.networknt.config.schema.IntegerField; // REQUIRED IMPORT
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

// <<< REQUIRED ANNOTATION FOR SCHEMA GENERATION >>>
@ConfigSchema(
        configKey = "latency-assault",
        configName = "latency-assault",
        configDescription = "Light Chaos Monkey Latency Assault Handler Configuration.",
        outputFormats = {OutputFormat.JSON_SCHEMA, OutputFormat.YAML}
)
public class LatencyAssaultConfig {
    private static final Logger logger = LoggerFactory.getLogger(LatencyAssaultConfig.class);

    public final static String CONFIG_NAME = "latency-assault";
    public final static String ENABLED = "enabled";
    public final static String BYPASS = "bypass";
    public final static String LEVEL = "level";
    public final static String LATENCY_RANGE_START = "latencyRangeStart";
    public final static String LATENCY_RANGE_END = "latencyRangeEnd";

    private Map<String, Object> mappedConfig;
    private final Config config;

    @BooleanField(
            configFieldName = ENABLED,
            externalizedKeyName = ENABLED,
            description = "Enable the handler if set to true so that it will be wired in the handler chain during the startup.",
            defaultValue = "false" // Matches the default in config file
    )
    boolean enabled;

    @BooleanField(
            configFieldName = BYPASS,
            externalizedKeyName = BYPASS,
            description = "Bypass the current chaos monkey middleware handler so that attacks won't be triggered.",
            defaultValue = "true" // Matches the default in config file
    )
    boolean bypass;

    @IntegerField(
            configFieldName = LEVEL,
            externalizedKeyName = LEVEL,
            description = "How many requests are to be attacked. 1 each request, 5 each 5th request is attacked.",
            defaultValue = "10" // Matches the default in config file
    )
    int level;

    @IntegerField(
            configFieldName = LATENCY_RANGE_START,
            externalizedKeyName = LATENCY_RANGE_START,
            description = "Dynamic Latency range start in milliseconds. When start and end are equal, then fixed latency.",
            defaultValue = "1000" // Matches the default in config file
    )
    int latencyRangeStart;

    @IntegerField(
            configFieldName = LATENCY_RANGE_END,
            externalizedKeyName = LATENCY_RANGE_END,
            description = "Dynamic latency range end in milliseconds.",
            defaultValue = "3000" // Matches the default in config file
    )
    int latencyRangeEnd;

    // --- Constructor and Loading Logic ---

    private LatencyAssaultConfig() {
        this(CONFIG_NAME);
    }

    private LatencyAssaultConfig(String configName) {
        config = Config.getInstance();
        mappedConfig = config.getJsonMapConfigNoCache(configName);
        setConfigData();
    }

    public static LatencyAssaultConfig load() {
        return new LatencyAssaultConfig();
    }

    public static LatencyAssaultConfig load(String configName) {
        return new LatencyAssaultConfig(configName);
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

        object = mappedConfig.get(LATENCY_RANGE_START);
        if(object != null) latencyRangeStart = Config.loadIntegerValue(LATENCY_RANGE_START, object);

        object = mappedConfig.get(LATENCY_RANGE_END);
        if(object != null) latencyRangeEnd = Config.loadIntegerValue(LATENCY_RANGE_END, object);
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
