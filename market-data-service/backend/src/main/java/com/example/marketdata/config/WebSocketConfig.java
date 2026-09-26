package com.example.marketdata.config;

import com.example.marketdata.ws.MarketWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import java.util.Map;

@Configuration
public class WebSocketConfig {
    @Bean
    HandlerMapping webSocketMapping(MarketWebSocketHandler handler) {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(-1);
        mapping.setUrlMap(Map.of("/ws/market", handler));
        return mapping;
    }
    @Bean WebSocketHandlerAdapter webSocketHandlerAdapter() { return new WebSocketHandlerAdapter(); }
}
