package com.networknt.chaos;

import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.config.JsonMapper;
import com.networknt.handler.LightHttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Update the configuration for chaos monkey handlers on the fly.
 *
 * @author Steve Hu
 */
public class ChaosMonkeyPostHandler implements LightHttpHandler {
    public static ChaosMonkeyConfig config = (ChaosMonkeyConfig) Config.getInstance().getJsonObjectConfig(ChaosMonkeyConfig.CONFIG_NAME, ChaosMonkeyConfig.class);
    private static final Logger logger = LoggerFactory.getLogger(ChaosMonkeyPostHandler.class);
    private static final String HANDLER_IS_DISABLED = "ERR10065";

    public ChaosMonkeyPostHandler() {
        logger.info("ChaosMonkeyPostHandler constructed");
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if(logger.isDebugEnabled()) logger.debug("ChaosMonkeyPostHandler.handleRequest starts.");
        if(config.isEnabled()) {
            String assault = exchange.getQueryParameters().get("assault").getFirst();
            Map<String, Object> bodyMap = (Map<String, Object>)exchange.getAttachment(BodyHandler.REQUEST_BODY);
            // set the config object directly with the public static variable.
            switch (assault) {
                case "com.networknt.chaos.ExceptionAssaultHandler":
                    ExceptionAssaultHandler.config = Config.getInstance().getMapper().convertValue(bodyMap, ExceptionAssaultConfig.class);
                    break;
                case "com.networknt.chaos.KillappAssaultHandler":
                    KillappAssaultHandler.config = Config.getInstance().getMapper().convertValue(bodyMap, KillappAssaultConfig.class);
                    break;
                case "com.networknt.chaos.LatencyAssaultHandler":
                    LatencyAssaultHandler.config = Config.getInstance().getMapper().convertValue(bodyMap, LatencyAssaultConfig.class);
                    break;
                case "com.networknt.chaos.MemoryAssaultHandler":
                    MemoryAssaultHandler.config = Config.getInstance().getMapper().convertValue(bodyMap, MemoryAssaultConfig.class);
                    break;
                default:
                    logger.error("Invalid assault " + assault);
            }
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            if(logger.isDebugEnabled()) logger.debug("ChaosMonkeyPostHandler.handleRequest ends.");
            exchange.getResponseSender().send(JsonMapper.toJson(bodyMap));
        } else {
            logger.error("Chaos Monkey API is disabled in chaos-monkey.yml");
            if(logger.isDebugEnabled()) logger.debug("ChaosMonkeyPostHandler.handleRequest ends with an error.");
            setExchangeStatus(exchange, HANDLER_IS_DISABLED, "Chaos Monkey");
        }
    }
}
