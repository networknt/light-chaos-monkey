package com.networknt.chaos;

import com.networknt.httpstring.AttachmentConstants;
import io.undertow.Undertow;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChaosMonkeyPostHandlerTest {
    @Test
    void bindsAllPostedAssaultConfigurationsThroughTheSharedMapper() throws Exception {
        var apiConfig = ChaosMonkeyConfig.load();
        boolean enabled = apiConfig.enabled;
        var exception = ExceptionAssaultHandler.config;
        var latency = LatencyAssaultHandler.config;
        var memory = MemoryAssaultHandler.config;
        var killapp = KillappAssaultHandler.config;
        Map<String, Map<String, Object>> bodies = Map.of(
                "Exception", Map.of("enabled", true, "bypass", false, "level", 3),
                "Killapp", Map.of("enabled", true, "bypass", false, "level", 4),
                "Latency", Map.of("enabled", true, "bypass", false, "level", 5,
                        "latencyRangeStart", 11, "latencyRangeEnd", 29),
                "Memory", Map.of("enabled", true, "bypass", false, "level", 6,
                        "memoryMillisecondsHoldFilledMemory", 17,
                        "memoryMillisecondsWaitNextIncrease", 23,
                        "memoryFillIncrementFraction", 0.125,
                        "memoryFillTargetFraction", 0.75));
        var handler = new ChaosMonkeyPostHandler();
        var server = Undertow.builder().setIoThreads(1).setWorkerThreads(2)
                .addHttpListener(0, "127.0.0.1")
                .setHandler(exchange -> {
                    String name = exchange.getQueryParameters().get("assault").getFirst();
                    String key = name.substring("com.networknt.chaos.".length(), name.length() - "AssaultHandler".length());
                    exchange.putAttachment(AttachmentConstants.REQUEST_BODY, bodies.get(key));
                    handler.handleRequest(exchange);
                }).build();
        try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) {
            apiConfig.enabled = true;
            server.start();
            int port = ((InetSocketAddress) server.getListenerInfo().getFirst().getAddress()).getPort();
            for (String key : bodies.keySet()) {
                var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                        + "/?assault=com.networknt.chaos." + key + "AssaultHandler"))
                        .timeout(Duration.ofSeconds(5)).POST(HttpRequest.BodyPublishers.noBody()).build();
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertEquals(200, response.statusCode(), response.body());
                assertTrue(response.body().contains("\"level\":"), response.body());
            }
            assertTrue(ExceptionAssaultHandler.config.isEnabled());
            assertFalse(ExceptionAssaultHandler.config.isBypass());
            assertEquals(3, ExceptionAssaultHandler.config.getLevel());
            assertTrue(KillappAssaultHandler.config.isEnabled());
            assertFalse(KillappAssaultHandler.config.isBypass());
            assertEquals(4, KillappAssaultHandler.config.getLevel());
            assertTrue(LatencyAssaultHandler.config.isEnabled());
            assertFalse(LatencyAssaultHandler.config.isBypass());
            assertEquals(5, LatencyAssaultHandler.config.getLevel());
            assertEquals(11, LatencyAssaultHandler.config.getLatencyRangeStart());
            assertEquals(29, LatencyAssaultHandler.config.getLatencyRangeEnd());
            assertTrue(MemoryAssaultHandler.config.isEnabled());
            assertFalse(MemoryAssaultHandler.config.isBypass());
            assertEquals(6, MemoryAssaultHandler.config.getLevel());
            assertEquals(17, MemoryAssaultHandler.config.getMemoryMillisecondsHoldFilledMemory());
            assertEquals(23, MemoryAssaultHandler.config.getMemoryMillisecondsWaitNextIncrease());
            assertEquals(0.125f, MemoryAssaultHandler.config.getMemoryFillIncrementFraction());
            assertEquals(0.75f, MemoryAssaultHandler.config.getMemoryFillTargetFraction());
        } finally {
            server.stop();
            apiConfig.enabled = enabled;
            ExceptionAssaultHandler.config = exception;
            LatencyAssaultHandler.config = latency;
            MemoryAssaultHandler.config = memory;
            KillappAssaultHandler.config = killapp;
        }
    }
}
