package com.example.marketdata.session;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {
    @Test void newLoginInvalidatesPreviousSession() {
        SessionManager m = new SessionManager();
        var first = m.login("admin", "admin123");
        var second = m.login("admin", "admin123");
        assertNotNull(first); assertNotNull(second);
        assertNull(m.find(first.token()));
        assertNotNull(m.find(second.token()));
    }
    @Test void invalidCredentialsRejected() {
        assertNull(new SessionManager().login("admin", "wrong"));
    }
}
