package com.networknt.chaos;

import com.networknt.config.Config;
import com.networknt.handler.Handler;
import com.networknt.handler.MiddlewareHandler;
import com.networknt.utility.ModuleRegistry;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class LatencyAssaultHandler implements MiddlewareHandler {
    public static LatencyAssaultConfig config = (LatencyAssaultConfig) Config.getInstance().getJsonObjectConfig(LatencyAssaultConfig.CONFIG_NAME, LatencyAssaultConfig.class);
    private static final Logger logger = LoggerFactory.getLogger(LatencyAssaultHandler.class);
    private volatile HttpHandler next;

    public LatencyAssaultHandler() {
        logger.info("LatencyAssaultHandler constructed");
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if(isEnabled() && isTrouble() && !config.isBypass()) {
            int i = determineLatency();
            logger.info("Chaos Monkey - I am sleeping for " + i + " milliseconds!");
            try {
                Thread.sleep(i);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        Handler.next(exchange, next);
    }

    @Override
    public HttpHandler getNext() {
        return next;
    }

    @Override
    public MiddlewareHandler setNext(final HttpHandler next) {
        Handlers.handlerNotNull(next);
        this.next = next;
        return this;
    }

    @Override
    public boolean isEnabled() {
        return config.isEnabled();
    }

    @Override
    public void register() {
        ModuleRegistry.registerModule(LatencyAssaultHandler.class.getName(), Config.getInstance().getJsonMapConfigNoCache(LatencyAssaultConfig.CONFIG_NAME), null);
    }

    private int determineLatency() {
        int latencyRangeStart = config.getLatencyRangeStart();
        int latencyRangeEnd = config.getLatencyRangeEnd();

        if (latencyRangeStart == latencyRangeEnd) {
            return latencyRangeStart;
        } else {
            return ThreadLocalRandom.current().nextInt(latencyRangeStart, latencyRangeEnd);
        }
    }

    @Override
    public void reload() {
        config = (LatencyAssaultConfig) Config.getInstance().getJsonObjectConfig(LatencyAssaultConfig.CONFIG_NAME, LatencyAssaultConfig.class);
    }


    private boolean isTrouble() {
        return getTroubleRandom() >= config.getLevel();
    }

    public int getTroubleRandom() {
        return ThreadLocalRandom.current().nextInt(1, config.getLevel() + 1);
    }

}
