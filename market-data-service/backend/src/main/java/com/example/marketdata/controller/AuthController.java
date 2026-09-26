package com.example.marketdata.controller;

import com.example.marketdata.model.*;
import com.example.marketdata.session.SessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SessionManager sessions;
    public AuthController(SessionManager sessions) { this.sessions = sessions; }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null || request.username() == null || request.password() == null) return ResponseEntity.badRequest().body("Username and password are required");
        var result = sessions.login(request.username(), request.password());
        return result == null ? ResponseEntity.status(401).body("Invalid credentials") : ResponseEntity.ok(new LoginResponse(result.token(), result.username()));
    }
    @PostMapping("/logout") public ResponseEntity<Void> logout(@RequestHeader(value="X-Session-Token", required=false) String token) { sessions.remove(token); return ResponseEntity.noContent().build(); }
}
