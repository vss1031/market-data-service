# Market Data Service – OKX Technical Assignment

## What this implements
- Public OKX REST integration for the top 20 SPOT pairs by 24h quote volume.
- Backend proxy/aggregator: browser never calls OKX directly.
- Login with a hardcoded user store (`admin/admin123`, `demo/demo123`).
- Single active session per username. A new login invalidates the previous token and its WebSocket.
- Market overview auto-refreshes through the backend every 5 seconds.
- Live order book through Browser -> Spring WebSocket -> OKX public WebSocket `books` channel.
- Top 15 bids and asks displayed.
- Old OKX connection is closed when the browser disconnects or switches symbol.

The assignment asks for a backend proxy, top 20 spot pairs, live order book, single-session enforcement, working source, README and a short explanation of session enforcement. This project covers those requirements.

## Prerequisites
- Java 21+
- Maven 3.9+
- Node.js 20+
- npm

## Run backend
```bash
cd backend
mvn spring-boot:run
```
Backend: http://localhost:8080

## Run frontend
```bash
cd frontend
npm install
npm run dev
```
Open the Vite URL, normally http://localhost:5173.

Demo credentials:
- admin / admin123
- demo / demo123

## API
`POST /api/auth/login`
```json
{"username":"admin","password":"admin123"}
```

`GET /api/markets/top20`

Browser WebSocket:
`ws://localhost:8080/ws/market?token=<session-token>`

Send:
```json
{"op":"subscribe","instId":"BTC-USDT"}
```

The backend opens a public OKX WebSocket and subscribes to `books` for that instrument. Incoming OKX messages are forwarded to the browser.

## Single-session enforcement
`SessionManager` keeps two concurrent maps: username -> current session and token -> current session. During login, any existing session for the same username is invalidated, removed from the token map, and sent a `session_replaced` event. Its OKX WebSocket is subsequently closed by the browser handler lifecycle. Therefore, only the latest login remains valid.

For a multi-instance production deployment, this in-memory mechanism should be moved to Redis (or another shared session store) so all application instances enforce the rule consistently.

## Architecture
```text
React Browser
   |
   | REST / WebSocket
   v
Spring Boot WebFlux
   |-- Auth / Session Manager
   |-- Market REST Proxy ----> OKX REST /market/tickers
   `-- Market WebSocket -----> OKX Public WS /books
```

## Assumptions / limitations
- The assignment permits a hardcoded user store, so no database or OAuth provider is used.
- This is an assessment implementation rather than a production trading platform.
- Market overview is refreshed from the backend every 5 seconds; the order book is event-streamed through WebSocket.
- `volCcy24h` is used as the displayed 24h volume so different base currencies can be compared on a common quote-currency basis.
- No trading/private OKX API is used, so API keys are not required.
- For horizontal scaling, session state and potentially OKX subscriptions should be externalized/shared.
