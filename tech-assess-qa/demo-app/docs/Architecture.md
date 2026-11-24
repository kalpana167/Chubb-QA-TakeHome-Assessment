# Application Architecture

```mermaid
flowchart LR
 U[Browser] -->|HTTP :3001| UI[Next.js 16 / React 19]
 UI -->|BFF API + CSRF| BFF[Spring Boot BFF :8090]
 BFF -->|OIDC / JWT| KC[Keycloak 26 :8180]
 BFF -->|REST| CS[Claims Service :8080]
 CS -->|JDBC| DB[(PostgreSQL 15)]
 BFF -->|Cache| R[(Redis 7)]
 CS -->|Claim events| K[Kafka]
 K -->|Consumer / updates| BFF
 BFF -->|WebSocket| UI
 CDC[Debezium – optional] -. optional CDC .-> K
```

## Responsibilities
- UI: login/signup, claims list, claim wizard, claim details.
- BFF: UI-facing API, authentication integration, orchestration, cache/event/WebSocket integration.
- Claims Service: claim domain operations, persistence and event publication.
- Keycloak: identity provider and OAuth2/OIDC tokens.
- PostgreSQL: durable data; Redis: cache; Kafka: asynchronous events.
- Debezium is optional; configured default is direct Kafka publishing.

## Trust boundaries
Browser↔BFF (authentication/CSRF/validation); BFF↔Claims Service (REST mapping/downstream errors); services↔Keycloak (JWT/JWK/roles); Claims Service↔PostgreSQL (persistence/ownership); services↔Kafka/Redis (events/cache).
