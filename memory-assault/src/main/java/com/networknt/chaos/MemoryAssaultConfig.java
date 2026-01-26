package com.networknt.chaos;

import com.networknt.config.Config;
import com.networknt.config.schema.ConfigSchema;
import com.networknt.config.schema.OutputFormat;
import com.networknt.config.schema.BooleanField;
import com.networknt.config.schema.IntegerField;
import com.networknt.config.schema.NumberField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.networknt.server.ModuleRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// <<< REQUIRED ANNOTATION FOR SCHEMA GENERATION >>>
@ConfigSchema(
        configKey = "memory-assault",
        configName = "memory-assault",
        configDescription = "Light Chaos Monkey Memory Assault Handler Configuration.",
        outputFormats = {OutputFormat.JSON_SCHEMA, OutputFormat.YAML}
)
public class MemoryAssaultConfig {
    private static final Logger logger = LoggerFactory.getLogger(MemoryAssaultConfig.class);

    public final static String CONFIG_NAME = "memory-assault";
    public final static String ENABLED = "enabled";
    public final static String BYPASS = "bypass";
    public final static String LEVEL = "level";
    public final static String MEMORY_HOLD_MS = "memoryMillisecondsHoldFilledMemory";
    public final static String MEMORY_WAIT_MS = "memoryMillisecondsWaitNextIncrease";
    public final static String MEMORY_FILL_INCREMENT_FRACTION = "memoryFillIncrementFraction";
    public final static String MEMORY_FILL_TARGET_FRACTION = "memoryFillTargetFraction";

    private Map<String, Object> mappedConfig;
    private final Config config;
    private static final Map<String, MemoryAssaultConfig> instances = new ConcurrentHashMap<>();

    @BooleanField(
            configFieldName = ENABLED,
            externalizedKeyName = ENABLED,
            description = "Enable the handler if set to true so that it will be wired in the handler chain during the startup.",
            defaultValue = "false"
    )
    boolean enabled;

    @BooleanField(
            configFieldName = BYPASS,
            externalizedKeyName = BYPASS,
            description = "Bypass the current chaos monkey middleware handler so that attacks won't be triggered.",
            defaultValue = "true"
    )
    boolean bypass;

    @IntegerField(
            configFieldName = LEVEL,
            externalizedKeyName = LEVEL,
            description = "How many requests are to be attacked. 1 each request, 5 each 5th request is attacked.",
            defaultValue = "10"
    )
    int level;

    @IntegerField(
            configFieldName = MEMORY_HOLD_MS,
            externalizedKeyName = MEMORY_HOLD_MS,
            description = "Duration to assault memory when requested fill amount is reached in ms.\nmin=1500, max=Integer.MAX_VALUE",
            defaultValue = "90000"
    )
    int memoryMillisecondsHoldFilledMemory;

    @IntegerField(
            configFieldName = MEMORY_WAIT_MS,
            externalizedKeyName = MEMORY_WAIT_MS,
            description = "Time in ms between increases of memory usage.\nmin=100,max=30000",
            defaultValue = "1000"
    )
    int memoryMillisecondsWaitNextIncrease;

    @NumberField( // Use DoubleField for float/double properties
            configFieldName = MEMORY_FILL_INCREMENT_FRACTION,
            externalizedKeyName = MEMORY_FILL_INCREMENT_FRACTION,
            description = "Fraction of one individual memory increase iteration. 1.0 equals 100 %.\nmin=0.01, max = 1.0",
            defaultValue = "0.15"
    )
    float memoryFillIncrementFraction; // Java type is float

    @NumberField( // Use DoubleField for float/double properties
            configFieldName = MEMORY_FILL_TARGET_FRACTION,
            externalizedKeyName = MEMORY_FILL_TARGET_FRACTION,
            description = "Final fraction of used memory by assault. 0.95 equals 95 %.\nmin=0.01, max = 0.95",
            defaultValue = "0.25"
    )
    float memoryFillTargetFraction; // Java type is float

    // --- Constructor and Loading Logic ---

    public MemoryAssaultConfig() {
        this(CONFIG_NAME);
    }

    private MemoryAssaultConfig(String configName) {
        config = Config.getInstance();
        mappedConfig = config.getJsonMapConfigNoCache(configName);
        setConfigData();
    }

    public static MemoryAssaultConfig load() {
        return load(CONFIG_NAME);
    }

    public static MemoryAssaultConfig load(String configName) {
        MemoryAssaultConfig instance = instances.get(configName);
        if (instance != null) {
            return instance;
        }
        synchronized (MemoryAssaultConfig.class) {
            instance = instances.get(configName);
            if (instance != null) {
                return instance;
            }
            instance = new MemoryAssaultConfig(configName);
            instances.put(configName, instance);
            if (CONFIG_NAME.equals(configName)) {
                ModuleRegistry.registerModule(CONFIG_NAME, MemoryAssaultConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), null);
            }
            return instance;
        }
    }

    public static void reload() {
        reload(CONFIG_NAME);
    }

    public static void reload(String configName) {
        synchronized (MemoryAssaultConfig.class) {
            MemoryAssaultConfig instance = new MemoryAssaultConfig(configName);
            instances.put(configName, instance);
            if (CONFIG_NAME.equals(configName)) {
                ModuleRegistry.registerModule(CONFIG_NAME, MemoryAssaultConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), null);
            }
        }
    }


    private void setConfigData() {
        Object object = mappedConfig.get(ENABLED);
        if(object != null) enabled = Config.loadBooleanValue(ENABLED, object);

        object = mappedConfig.get(BYPASS);
        if(object != null) bypass = Config.loadBooleanValue(BYPASS, object);

        object = mappedConfig.get(LEVEL);
        if(object != null) level = Config.loadIntegerValue(LEVEL, object);

        object = mappedConfig.get(MEMORY_HOLD_MS);
        if(object != null) memoryMillisecondsHoldFilledMemory = Config.loadIntegerValue(MEMORY_HOLD_MS, object);

        object = mappedConfig.get(MEMORY_WAIT_MS);
        if(object != null) memoryMillisecondsWaitNextIncrease = Config.loadIntegerValue(MEMORY_WAIT_MS, object);

        // Load double/float values using loadDoubleValue and cast to float
        object = mappedConfig.get(MEMORY_FILL_INCREMENT_FRACTION);
        if(object != null) memoryFillIncrementFraction = Config.loadDoubleValue(MEMORY_FILL_INCREMENT_FRACTION, object).floatValue();

        object = mappedConfig.get(MEMORY_FILL_TARGET_FRACTION);
        if(object != null) memoryFillTargetFraction = Config.loadDoubleValue(MEMORY_FILL_TARGET_FRACTION, object).floatValue();
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

    public int getMemoryMillisecondsHoldFilledMemory() {
        return memoryMillisecondsHoldFilledMemory;
    }

    public void setMemoryMillisecondsHoldFilledMemory(int memoryMillisecondsHoldFilledMemory) {
        this.memoryMillisecondsHoldFilledMemory = memoryMillisecondsHoldFilledMemory;
    }

    public int getMemoryMillisecondsWaitNextIncrease() {
        return memoryMillisecondsWaitNextIncrease;
    }

    public void setMemoryMillisecondsWaitNextIncrease(int memoryMillisecondsWaitNextIncrease) {
        this.memoryMillisecondsWaitNextIncrease = memoryMillisecondsWaitNextIncrease;
    }

    public float getMemoryFillIncrementFraction() {
        return memoryFillIncrementFraction;
    }

    public void setMemoryFillIncrementFraction(float memoryFillIncrementFraction) {
        this.memoryFillIncrementFraction = memoryFillIncrementFraction;
    }

    public float getMemoryFillTargetFraction() {
        return memoryFillTargetFraction;
    }

    public void setMemoryFillTargetFraction(float memoryFillTargetFraction) {
        this.memoryFillTargetFraction = memoryFillTargetFraction;
    }
}
