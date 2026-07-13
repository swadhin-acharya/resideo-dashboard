package com.openqa.dashboard.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class LiveExecutionHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(LiveExecutionHandler.class);
    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final Map<String, UUID> sessionExecutions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionFilters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public LiveExecutionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.debug("WebSocket connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (payload.startsWith("subscribe:")) {
            UUID executionId = UUID.fromString(payload.substring(10));
            sessionExecutions.put(session.getId(), executionId);
            sessionFilters.remove(session.getId());
        } else if (payload.startsWith("subscribe-dashboard")) {
            sessionExecutions.remove(session.getId());
            sessionFilters.put(session.getId(), "dashboard");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        sessionExecutions.remove(session.getId());
        sessionFilters.remove(session.getId());
        log.debug("WebSocket disconnected: {}", session.getId());
    }

    public void broadcast(UUID executionId, String type, Object data) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                "type", type,
                "executionId", executionId.toString(),
                "data", data,
                "timestamp", System.currentTimeMillis()
            ));
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    UUID sub = sessionExecutions.get(session.getId());
                    String filter = sessionFilters.get(session.getId());
                    if (sub == null || sub.equals(executionId) || "dashboard".equals(filter)) {
                        try {
                            session.sendMessage(message);
                        } catch (IOException e) {
                            log.warn("Failed to send WS message to {}", session.getId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket message", e);
        }
    }

    public void broadcastFeatureStart(UUID executionId, String featureName) {
        broadcast(executionId, "FEATURE_START", Map.of(
            "featureName", featureName
        ));
    }

    public void broadcastFeatureComplete(UUID executionId, String featureName, String status, int passed, int failed) {
        broadcast(executionId, "FEATURE_COMPLETE", Map.of(
            "featureName", featureName,
            "status", status,
            "passed", passed,
            "failed", failed
        ));
    }

    public void broadcastScenarioStart(UUID executionId, String featureName, String scenarioName) {
        broadcast(executionId, "SCENARIO_START", Map.of(
            "featureName", featureName,
            "scenarioName", scenarioName
        ));
    }

    public void broadcastScenarioComplete(UUID executionId, String featureName, String scenarioName, String status) {
        broadcast(executionId, "SCENARIO_COMPLETE", Map.of(
            "featureName", featureName,
            "scenarioName", scenarioName,
            "status", status
        ));
    }

    public void broadcastProgress(UUID executionId, int completed, int total) {
        broadcast(executionId, "PROGRESS", Map.of(
            "completed", completed,
            "total", total,
            "percent", total > 0 ? (completed * 100 / total) : 0
        ));
    }

    public void broadcastSummaryUpdate(UUID executionId, int passed, int failed, int skipped) {
        broadcast(executionId, "SUMMARY_UPDATE", Map.of(
            "passed", passed,
            "failed", failed,
            "skipped", skipped
        ));
    }
}
