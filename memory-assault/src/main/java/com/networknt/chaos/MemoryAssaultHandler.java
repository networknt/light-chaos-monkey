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

import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryAssaultHandler implements MiddlewareHandler {
    public static MemoryAssaultConfig config = (MemoryAssaultConfig) Config.getInstance().getJsonObjectConfig(MemoryAssaultConfig.CONFIG_NAME, MemoryAssaultConfig.class);
    private static final Logger logger = LoggerFactory.getLogger(MemoryAssaultHandler.class);
    private static final AtomicLong stolenMemory = new AtomicLong(0);

    private volatile HttpHandler next;
    private Runtime runtime = Runtime.getRuntime();

    public MemoryAssaultHandler() {
        logger.info("MemoryAssaultHandler constructed");
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if(isEnabled() && isTrouble() && !config.isBypass()) {
            logger.info("Chaos Monkey - I am eating free memory!");
            eatFreeMemory();
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
        ModuleRegistry.registerModule(MemoryAssaultHandler.class.getName(), Config.getInstance().getJsonMapConfigNoCache(MemoryAssaultConfig.CONFIG_NAME), null);
    }

    private void eatFreeMemory() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        Vector<byte[]> memoryVector = new Vector<>();

        long stolenMemoryTotal = 0L;

        while (isEnabled()) {
            // overview of memory methods in java https://stackoverflow.com/a/18375641
            long freeMemory = runtime.freeMemory();
            long usedMemory = runtime.totalMemory() - freeMemory;

            if (cannotAllocateMoreMemory()) {
                logger.debug("Cannot allocate more memory");
                break;
            }

            logger.debug("Used memory in bytes: " + usedMemory);

            stolenMemoryTotal = stealMemory(memoryVector, stolenMemoryTotal, getBytesToSteal());
            waitUntil(config.getMemoryMillisecondsWaitNextIncrease());
        }

        // Hold memory level and cleanUp after, only if experiment is running
        if (isEnabled()) {
            logger.info("Memory fill reached, now sleeping and holding memory");
            waitUntil(config.getMemoryMillisecondsHoldFilledMemory());
        }

        // clean Vector
        memoryVector.clear();
        // quickly run gc for reuse
        runtime.gc();

        long stolenAfterComplete = stolenMemory.addAndGet(-stolenMemoryTotal);
    }

    private boolean cannotAllocateMoreMemory() {
        double limit =
                runtime.maxMemory() * config.getMemoryFillTargetFraction();
        return runtime.totalMemory() > Math.floor(limit);
    }

    private int getBytesToSteal() {
        int amount = (int)(runtime.freeMemory()
                                * config.getMemoryFillIncrementFraction());
        boolean isJava8 = System.getProperty("java.version").startsWith("1.8");
        // seems filling more than 256 MB per slice is bad on java 8
        // we keep running into heap errors and other OOMs.
        return isJava8 ? Math.min(MemorySizeConverter.toBytes(256), amount) : amount;
    }

    private long stealMemory(Vector<byte[]> memoryVector, long stolenMemoryTotal, int bytesToSteal) {
        memoryVector.add(createDirtyMemorySlice(bytesToSteal));

        stolenMemoryTotal += bytesToSteal;
        long newStolenTotal = stolenMemory.addAndGet(bytesToSteal);
        logger.debug(
                "Chaos Monkey - memory assault increase, free memory: "
                        + MemorySizeConverter.toMegabytes(runtime.freeMemory()));

        return stolenMemoryTotal;
    }

    private byte[] createDirtyMemorySlice(int size) {
        byte[] b = new byte[size];
        for (int idx = 0; idx < size; idx += 4096) { // 4096
            // is commonly the size of a memory page, forcing a commit
            b[idx] = 19;
        }

        return b;
    }

    private void waitUntil(int ms) {
        final long startNano = System.nanoTime();
        long now = startNano;
        while (startNano + TimeUnit.MILLISECONDS.toNanos(ms) > now && isEnabled()) {
            try {
                long elapsed = TimeUnit.NANOSECONDS.toMillis(startNano - now);
                Thread.sleep(Math.min(100, ms - elapsed));
                now = System.nanoTime();
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private boolean isTrouble() {
        return getTroubleRandom() >= config.getLevel();
    }

    public int getTroubleRandom() {
        return ThreadLocalRandom.current().nextInt(1, config.getLevel() + 1);
    }

}
