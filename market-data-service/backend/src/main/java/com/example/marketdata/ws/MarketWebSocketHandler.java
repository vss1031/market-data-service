package com.example.marketdata.ws;

import com.example.marketdata.session.ClientSession;
import com.example.marketdata.session.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MarketWebSocketHandler implements WebSocketHandler {
    private final SessionManager sessions;
    private final ObjectMapper mapper;
    private final String okxUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public MarketWebSocketHandler(SessionManager sessions, ObjectMapper mapper,
                                  @Value("${okx.websocket-url}") String okxUrl) {
        this.sessions = sessions; this.mapper = mapper; this.okxUrl = okxUrl;
    }

    @Override
    public Mono<Void> handle(WebSocketSession browser) {
        String token = browser.getHandshakeInfo().getUri().getQuery();
        token = extractToken(token);
        ClientSession client = sessions.find(token);
        if (client == null || !client.valid()) return browser.close();

        OkxConnection okx = new OkxConnection(client);
        Mono<Void> incoming = browser.receive().map(WebSocketMessage::getPayloadAsText).doOnNext(msg -> handleCommand(okx, msg)).then();
        Mono<Void> outgoing = browser.send(client.events().asFlux().map(browser::textMessage));
        return Mono.firstWithSignal(incoming, outgoing).doFinally(s -> okx.close());
    }

    private void handleCommand(OkxConnection okx, String message) {
        try {
            JsonNode n = mapper.readTree(message);
            String instId = n.path("instId").asText("");
            if ("subscribe".equals(n.path("op").asText()) && instId.matches("[A-Z0-9]+-[A-Z0-9]+")) okx.subscribe(instId);
        } catch (Exception ignored) { }
    }

    private String extractToken(String query) {
        if (query == null) return null;
        for (String p : query.split("&")) if (p.startsWith("token=")) return p.substring(6);
        return null;
    }

    private final class OkxConnection {
        private final ClientSession client;
        private final AtomicReference<WebSocket> socket = new AtomicReference<>();
        private volatile String current;
        OkxConnection(ClientSession client) { this.client = client; }

        void subscribe(String instId) {
            if (!client.valid()) return;
            WebSocket old = socket.getAndSet(null);
            if (old != null) old.sendClose(WebSocket.NORMAL_CLOSURE, "switch symbol");
            current = instId;
            httpClient.newWebSocketBuilder().buildAsync(URI.create(okxUrl), new WebSocket.Listener() {
                private final StringBuilder buffer = new StringBuilder();
                @Override public void onOpen(WebSocket ws) {
                    socket.set(ws);
                    String sub = "{\"op\":\"subscribe\",\"args\":[{\"channel\":\"books\",\"instId\":\"" + instId + "\"}]}";
                    ws.sendText(sub, true); ws.request(1);
                }
                @Override public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                    buffer.append(data);
                    if (last) { client.events().tryEmitNext(buffer.toString()); buffer.setLength(0); }
                    ws.request(1); return null;
                }
                @Override public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) { socket.compareAndSet(ws, null); return null; }
                @Override public void onError(WebSocket ws, Throwable error) { socket.compareAndSet(ws, null); client.events().tryEmitNext("{\"type\":\"upstream_error\",\"message\":\"OKX connection error\"}"); }
            });
        }
        void close() { WebSocket ws = socket.getAndSet(null); if (ws != null) ws.sendClose(WebSocket.NORMAL_CLOSURE, "client disconnected"); }
    }
}
