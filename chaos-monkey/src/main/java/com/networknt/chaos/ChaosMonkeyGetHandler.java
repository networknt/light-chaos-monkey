package com.networknt.chaos;

import com.networknt.config.JsonMapper;
import com.networknt.handler.LightHttpHandler;
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
    private static final Logger logger = LoggerFactory.getLogger(ChaosMonkeyGetHandler.class);
    public static ChaosMonkeyConfig config;

    public ChaosMonkeyGetHandler() {
        logger.info("ChaosMonkeyGetHandler constructed");
        config = ChaosMonkeyConfig.load();
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if(logger.isDebugEnabled()) logger.debug("ChaosMonkeyGetHandler.handleRequest starts.");
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ExceptionAssaultHandler.class.getName(), ExceptionAssaultHandler.config != null ? ExceptionAssaultHandler.config : ExceptionAssaultConfig.load());
        configMap.put(KillappAssaultHandler.class.getName(), KillappAssaultHandler.config != null ? KillappAssaultHandler.config : KillappAssaultConfig.load());
        configMap.put(LatencyAssaultHandler.class.getName(), LatencyAssaultHandler.config != null ? LatencyAssaultHandler.config : LatencyAssaultConfig.load());
        configMap.put(MemoryAssaultHandler.class.getName(), MemoryAssaultHandler.config != null ? MemoryAssaultHandler.config : MemoryAssaultConfig.load());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        if(logger.isDebugEnabled()) logger.debug("ChaosMonkeyGetHandler.handleRequest ends.");
        exchange.getResponseSender().send(JsonMapper.toJson(configMap));
    }
}
