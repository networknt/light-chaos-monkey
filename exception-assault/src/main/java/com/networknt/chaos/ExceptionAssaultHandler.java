package com.networknt.chaos;

import com.networknt.config.Config;
import com.networknt.handler.Handler;
import com.networknt.handler.MiddlewareHandler;
import com.networknt.handler.Handler;
import com.networknt.handler.MiddlewareHandler;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class ExceptionAssaultHandler implements MiddlewareHandler {
    public static ExceptionAssaultConfig config;
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAssaultHandler.class);
    private volatile HttpHandler next;

    public ExceptionAssaultHandler() {
        config = ExceptionAssaultConfig.load();
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

    private boolean isTrouble() {
        return getTroubleRandom() >= config.getLevel();
    }

    public int getTroubleRandom() {
        return ThreadLocalRandom.current().nextInt(1, config.getLevel() + 1);
    }


}
