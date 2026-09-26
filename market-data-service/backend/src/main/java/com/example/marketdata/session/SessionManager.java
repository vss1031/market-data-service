package com.example.marketdata.session;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {
    private final Map<String, ClientSession> byUsername = new ConcurrentHashMap<>();
    private final Map<String, ClientSession> byToken = new ConcurrentHashMap<>();
    private final Map<String, String> users = Map.of("admin", "admin123", "demo", "demo123");

    public LoginResult login(String username, String password) {
        if (!users.containsKey(username) || !users.get(username).equals(password)) return null;
        ClientSession previous = byUsername.remove(username);
        if (previous != null) { byToken.remove(previous.token()); previous.invalidate(); }
        String token = UUID.randomUUID().toString();
        ClientSession current = new ClientSession(username, token);
        byUsername.put(username, current); byToken.put(token, current);
        return new LoginResult(token, username);
    }
    public ClientSession find(String token) { return token == null ? null : byToken.get(token); }
    public void remove(String token) {
        ClientSession s = byToken.remove(token);
        if (s != null) byUsername.remove(s.username(), s);
    }
    public record LoginResult(String token, String username) {}
}
