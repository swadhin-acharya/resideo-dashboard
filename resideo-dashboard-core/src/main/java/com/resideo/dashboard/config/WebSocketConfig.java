package com.resideo.dashboard.config;

import com.resideo.dashboard.websocket.LiveExecutionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final LiveExecutionHandler liveExecutionHandler;

    public WebSocketConfig(LiveExecutionHandler liveExecutionHandler) {
        this.liveExecutionHandler = liveExecutionHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(liveExecutionHandler, "/ws/executions")
                .setAllowedOrigins("*");
    }
}
