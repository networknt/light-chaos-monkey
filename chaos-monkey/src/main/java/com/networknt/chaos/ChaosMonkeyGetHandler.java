package com.networknt.chaos;

import com.networknt.config.Config;
import com.networknt.config.JsonMapper;
import com.networknt.handler.LightHttpHandler;
import com.networknt.utility.ModuleRegistry;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Get all the configuration from chaos monkey handlers
 *
 * @author Steve Hu
 *
 */
public class ChaosMonkeyGetHandler implements LightHttpHandler {
    public static ChaosMonkeyConfig config = (ChaosMonkeyConfig) Config.getInstance().getJsonObjectConfig(ChaosMonkeyConfig.CONFIG_NAME, ChaosMonkeyConfig.class);
    private static final Logger logger = LoggerFactory.getLogger(ChaosMonkeyGetHandler.class);

    public ChaosMonkeyGetHandler() {
        logger.info("ChaosMonkeyGetHandler constructed");
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        Map<String, Object> registry = ModuleRegistry.getRegistry();
        configMap.put(ExceptionAssaultHandler.class.getName(), ExceptionAssaultHandler.config);
        configMap.put(KillappAssaultHandler.class.getName(), KillappAssaultHandler.config);
        configMap.put(LatencyAssaultHandler.class.getName(), LatencyAssaultHandler.config);
        configMap.put(MemoryAssaultHandler.class.getName(), MemoryAssaultHandler.config);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(JsonMapper.toJson(configMap));
    }
}
