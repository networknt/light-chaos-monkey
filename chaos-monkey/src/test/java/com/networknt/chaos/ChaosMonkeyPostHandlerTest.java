package com.networknt.chaos;

import com.networknt.body.BodyHandler;
import com.networknt.config.JsonMapper;
import io.undertow.Undertow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
    private ChaosMonkeyConfig apiConfig;
    private boolean enabled;
    private ExceptionAssaultConfig exception;
    private LatencyAssaultConfig latency;
    private MemoryAssaultConfig memory;
    private KillappAssaultConfig killapp;
    private Undertow server;
    private HttpClient client;
    private String endpoint;

    @BeforeEach
    void startServer() {
        apiConfig = ChaosMonkeyConfig.load();
        enabled = apiConfig.enabled;
        exception = ExceptionAssaultHandler.config;
        latency = LatencyAssaultHandler.config;
        memory = MemoryAssaultHandler.config;
        killapp = KillappAssaultHandler.config;
        var body = new BodyHandler();
        body.setNext(new ChaosMonkeyPostHandler());
        server = Undertow.builder().setIoThreads(1).setWorkerThreads(2)
                .addHttpListener(0, "127.0.0.1").setHandler(body).build();
        apiConfig.enabled = true;
        server.start();
        int port = ((InetSocketAddress) server.getListenerInfo().getFirst().getAddress()).getPort();
        endpoint = "http://127.0.0.1:" + port + "/?assault=com.networknt.chaos.";
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @AfterEach
    void stopServerAndRestoreConfigurations() {
        try {
            if (client != null) client.close();
            if (server != null) server.stop();
        } finally {
            apiConfig.enabled = enabled;
            ExceptionAssaultHandler.config = exception;
            LatencyAssaultHandler.config = latency;
            MemoryAssaultHandler.config = memory;
            KillappAssaultHandler.config = killapp;
        }
    }

    private HttpResponse<String> post(String assault, String json) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(endpoint + assault + "AssaultHandler"))
                .timeout(Duration.ofSeconds(5));
        if (json != null) request.header("Content-Type", "application/json");
        return client.send(request.POST(json == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private void assertConfigurationsUnchanged() {
        assertSame(exception, ExceptionAssaultHandler.config);
        assertSame(latency, LatencyAssaultHandler.config);
        assertSame(memory, MemoryAssaultHandler.config);
        assertSame(killapp, KillappAssaultHandler.config);
    }

    // The existing POST handler uses ERR10001 (HTTP 401 in the core catalog)
    // for unknown assaults and absent bodies. This PR preserves that behavior.
    @Test
    void rejectsUnknownAssaultWithoutChangingConfigurations() throws Exception {
        var response = post("Unknown", "{\"level\":3}");
        assertEquals(401, response.statusCode(), response.body());
        assertTrue(response.body().contains("ERR10001"), response.body());
        assertConfigurationsUnchanged();
    }

    @Test
    void rejectsMissingBodyWithoutChangingConfigurations() throws Exception {
        var response = post("Latency", null);
        assertEquals(401, response.statusCode(), response.body());
        assertTrue(response.body().contains("ERR10001"), response.body());
        assertConfigurationsUnchanged();
    }

    @Test
    void rejectsEmptyJsonDocumentWithoutChangingConfigurations() throws Exception {
        var response = post("Latency", "");
        assertEquals(401, response.statusCode(), response.body());
        assertTrue(response.body().contains("ERR10001"), response.body());
        assertConfigurationsUnchanged();
    }

    @Test
    void rejectsMalformedJsonWithoutChangingConfigurations() throws Exception {
        var response = post("Latency", "{\"level\":");
        assertEquals(400, response.statusCode(), response.body());
        assertTrue(response.body().contains("ERR10015"), response.body());
        assertConfigurationsUnchanged();
    }

    @Test
    void rejectsNonNumericLevelWithoutReplacingTheConfiguration() throws Exception {
        var response = post("Latency", "{\"level\":\"not-a-number\"}");
        // Existing handler behavior: conversion errors propagate to Undertow.
        assertEquals(500, response.statusCode(), response.body());
        assertConfigurationsUnchanged();
    }

    @Test
    void preservesNumericStringCoercion() throws Exception {
        var response = post("Latency", "{\"level\":\"7\"}");
        assertEquals(200, response.statusCode(), response.body());
        assertEquals(7, LatencyAssaultHandler.config.getLevel());
    }

    @Test
    void emptyObjectKeepsConfiguredDefaults() throws Exception {
        var defaults = new LatencyAssaultConfig();
        var response = post("Latency", "{}");
        assertEquals(200, response.statusCode(), response.body());
        var actual = LatencyAssaultHandler.config;
        assertEquals(defaults.isEnabled(), actual.isEnabled());
        assertEquals(defaults.isBypass(), actual.isBypass());
        assertEquals(defaults.getLevel(), actual.getLevel());
        assertEquals(defaults.getLatencyRangeStart(), actual.getLatencyRangeStart());
        assertEquals(defaults.getLatencyRangeEnd(), actual.getLatencyRangeEnd());
    }

    @Test
    void bindsAllPostedAssaultConfigurationsThroughTheSharedMapper() throws Exception {
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
        for (String key : bodies.keySet()) {
            var response = post(key, JsonMapper.toJson(bodies.get(key)));
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
    }
}
