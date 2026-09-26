package com.example.marketdata.session;

import reactor.core.publisher.Sinks;

public final class ClientSession {
    private final String username;
    private final String token;
    private final Sinks.Many<String> events = Sinks.many().multicast().onBackpressureBuffer();
    private volatile boolean valid = true;

    public ClientSession(String username, String token) { this.username = username; this.token = token; }
    public String username() { return username; }
    public String token() { return token; }
    public Sinks.Many<String> events() { return events; }
    public boolean valid() { return valid; }
    public void invalidate() { valid = false; events.tryEmitNext("{\"type\":\"session_replaced\"}"); events.tryEmitComplete(); }
}
