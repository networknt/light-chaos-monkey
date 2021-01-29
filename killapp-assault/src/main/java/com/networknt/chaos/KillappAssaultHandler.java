package com.networknt.chaos;

import com.networknt.config.Config;
import com.networknt.handler.Handler;
import com.networknt.handler.MiddlewareHandler;
import com.networknt.utility.ModuleRegistry;
import com.networknt.server.Server;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class KillappAssaultHandler implements MiddlewareHandler {
    public static KillappAssaultConfig config = (KillappAssaultConfig) Config.getInstance().getJsonObjectConfig(KillappAssaultConfig.CONFIG_NAME, KillappAssaultConfig.class);
    private static final Logger logger = LoggerFactory.getLogger(KillappAssaultHandler.class);
    private volatile HttpHandler next;

    public KillappAssaultHandler() {
        logger.info("KillappAssaultHandler constructed");
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if(isEnabled() && isTrouble()) {
            try {
                logger.info("Chaos Monkey - I am killing the Server!");
                Server.shutdown();
                Thread.sleep(3000); // wait before shutdown hooks to complete
                System.exit(0);
            } catch (Exception e) {
                logger.info("Chaos Monkey - Unable to kill the Server!");
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
        ModuleRegistry.registerModule(KillappAssaultHandler.class.getName(), Config.getInstance().getJsonMapConfigNoCache(KillappAssaultConfig.CONFIG_NAME), null);
    }

    private boolean isTrouble() {
        return getTroubleRandom() >= config.getLevel();
    }

    public int getTroubleRandom() {
        return ThreadLocalRandom.current().nextInt(1, config.getLevel() + 1);
    }

}
