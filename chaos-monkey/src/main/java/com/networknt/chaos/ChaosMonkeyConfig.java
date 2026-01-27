/*
 * Copyright (c) 2019 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.chaos;

import com.networknt.config.Config;
import com.networknt.config.schema.ConfigSchema; // <<< REQUIRED IMPORT
import com.networknt.config.schema.OutputFormat; // <<< REQUIRED IMPORT
import com.networknt.config.schema.BooleanField; // <<< REQUIRED IMPORT
import com.networknt.config.schema.NumberField; // <<< REQUIRED IMPORT
import com.networknt.config.schema.StringField; // <<< REQUIRED IMPORT
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.networknt.server.ModuleRegistry;
import java.util.Map;

// <<< REQUIRED ANNOTATION FOR SCHEMA GENERATION >>>
@ConfigSchema(
        configKey = "chaos-monkey",
        configName = "chaos-monkey",
        configDescription = "Light Chaos Monkey API handlers Configuration.",
        outputFormats = {OutputFormat.JSON_SCHEMA, OutputFormat.YAML}
)
public class ChaosMonkeyConfig {
    private static final Logger logger = LoggerFactory.getLogger(ChaosMonkeyConfig.class);

    public final static String CONFIG_NAME = "chaos-monkey";
    public final static String ENABLED = "enabled";


    @BooleanField(
            configFieldName = ENABLED,
            externalizedKeyName = ENABLED,
            description = "Enable the handlers if set to true to allow user to get or post configurations for the assault handlers.",
            defaultValue = "false"
    )
    boolean enabled;

    private final Map<String, Object> mappedConfig;
    private static ChaosMonkeyConfig instance;

    public ChaosMonkeyConfig() {
        this(CONFIG_NAME);
    }

    private ChaosMonkeyConfig(String configName) {
        mappedConfig = Config.getInstance().getJsonMapConfig(configName);
        setConfigData();
    }

    public static ChaosMonkeyConfig load() {
        return load(CONFIG_NAME);
    }

    public static ChaosMonkeyConfig load(String configName) {
        if (CONFIG_NAME.equals(configName)) {
            Map<String, Object> mappedConfig = Config.getInstance().getJsonMapConfig(configName);
            if (instance != null && instance.getMappedConfig() == mappedConfig) {
                return instance;
            }
            synchronized (ChaosMonkeyConfig.class) {
                mappedConfig = Config.getInstance().getJsonMapConfig(configName);
                if (instance != null && instance.getMappedConfig() == mappedConfig) {
                    return instance;
                }
                instance = new ChaosMonkeyConfig(configName);
                ModuleRegistry.registerModule(CONFIG_NAME, ChaosMonkeyConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), null);
                return instance;
            }
        }
        return new ChaosMonkeyConfig(configName);
    }



    private void setConfigData() {
        Object object = mappedConfig.get(ENABLED);
        if(object != null) enabled = Config.loadBooleanValue(ENABLED, object);
    }

    public Map<String, Object> getMappedConfig() {
        return mappedConfig;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
