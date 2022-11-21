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

public class ExceptionAssaultHandler implements MiddlewareHandler {
    public static ExceptionAssaultConfig config = (ExceptionAssaultConfig) Config.getInstance().getJsonObjectConfig(ExceptionAssaultConfig.CONFIG_NAME, ExceptionAssaultConfig.class);
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAssaultHandler.class);
    private volatile HttpHandler next;

    public ExceptionAssaultHandler() {
        logger.info("ExceptionAssaultHandler constructed");
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if(logger.isDebugEnabled()) logger.debug("ExceptionAssaultHandler.handleRequest starts.");
        if(isEnabled() && isTrouble() && !config.isBypass()) {
            logger.info("Chaos Monkey - I am throwing an AssaultException!");
            throw new AssaultException("Chaos Monday AssaultException");
        }
        if(logger.isDebugEnabled()) logger.debug("ExceptionAssaultHandler.handleRequest ends.");
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
        ModuleRegistry.registerModule(ExceptionAssaultHandler.class.getName(), Config.getInstance().getJsonMapConfigNoCache(ExceptionAssaultConfig.CONFIG_NAME), null);
    }

    @Override
    public void reload() {
        config = (ExceptionAssaultConfig) Config.getInstance().getJsonObjectConfig(ExceptionAssaultConfig.CONFIG_NAME, ExceptionAssaultConfig.class);
    }

    private boolean isTrouble() {
        return getTroubleRandom() >= config.getLevel();
    }

    public int getTroubleRandom() {
        return ThreadLocalRandom.current().nextInt(1, config.getLevel() + 1);
    }


}
