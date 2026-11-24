# Test Strategy – Claims Management Application

## Objective
Risk-based testing of authentication, claim submission, claim retrieval, API security, and cross-service behaviour within the assessment timebox.

## System under test
- Next.js 16 / React 19 UI (3001)
- Spring Boot 4 / Java 21 BFF (8090)
- Spring Boot 4 / Java 21 Claims Service (8080)
- Keycloak 26 OAuth2/OIDC (8180)
- PostgreSQL 15, Redis 7, Kafka; optional Debezium CDC
- WebSocket updates from backend to UI

## Highest-risk areas
1. Authentication, session invalidation and role-based access.
2. Claim creation, persistence, retrieval and user ownership.
3. UI → BFF → Claims Service integration and JWT validation.
4. Required fields, lengths, date and amount boundaries.
5. Kafka event delivery, WebSocket updates and cache invalidation.

## Scope
**In:** UI login/signup and claim flow; component tests; Playwright smoke tests; BFF/Claims Service unit tests; API auth/CSRF checks; OpenAPI/security source review.

**Out/deferred:** full admin matrix; exhaustive endpoint contract testing; authenticated API boundary-value matrix; Kafka/WebSocket resilience and load; CDC integration; database migration/restore; cross-browser and visual regression.

## Approach
JUnit/Mockito for backend unit tests; Vitest/Testing Library for UI; Playwright POM/fixtures for E2E; curl exploratory API checks; manual UI exploration.

## Entry criteria
Docker Compose services running; health endpoints UP; UI reachable; test account available; test data isolated.

## Exit criteria
Targeted suites pass; core claim flow demonstrated; findings recorded; gaps/evidence limitations disclosed.

## Observed results
- BFF unit tests: 8 passed.
- Claims Service unit tests: 5 passed; Checkstyle 0 violations.
- UI unit/component tests: 12 passed.
- Playwright: 6 passed.
- Lint: 0 errors, 8 warnings.
- UI production build: successful.
- Manual UI: signup/login, form validation, claim submission and details verified; fresh Incognito protected-route redirect verified.
- API exploratory: GET claims without token 401; invalid login 401; empty login body 400; POST without CSRF header 403; POST with CSRF cookie/header but no bearer token 401.

## Risks and limitations
API checks were curl-based, not a complete automated schema suite. Authenticated claim-field boundary tests remain outstanding. Logout behaviour differed in a reused browser session versus fresh Incognito and is not a confirmed defect. Playwright video was not captured because FFmpeg was unavailable. Separate official test-case/defect templates were not located in the available supplied materials.
