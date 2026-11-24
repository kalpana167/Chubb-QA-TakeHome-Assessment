# Defect / Quality Findings

## QF-001 – BFF OpenAPI server URL points to wrong port
- Type: API contract/documentation; Severity: Low; Priority: Medium.
- Component: BFF OpenAPI.
- Evidence: `bff-api.yml` declares server URL `http://localhost:8080`; BFF listens on 8090 and Compose publishes 8090.
- Expected: OpenAPI server URL identifies BFF port 8090 (or uses relative/environment-specific URL).
- Actual: OpenAPI advertises port 8080, which is the Claims Service port.
- Impact: Swagger/API consumers may target the wrong service.
- Reproduction: Compare `servers.url` in `code/bff-service/src/main/resources/openapi/bff-api.yml` with BFF `server.port` and Compose mapping.
- Status: Needs owner confirmation; source-review evidence, no screenshot.

## QF-002 – Claim creation example omits required fields
- Type: API contract/documentation; Severity: Low; Priority: Medium.
- Component: `POST /api/claims`.
- Evidence: Example contains `incidentDate` and `description`; schema requires `incidentDate`, `incidentLocation`, `description`, `claimAmount`.
- Expected: Example satisfies the declared request schema.
- Actual: Example omits `incidentLocation` and `claimAmount`.
- Impact: Consumers may copy a payload that fails validation.
- Status: Needs owner confirmation; source-review evidence.

## OBS-003 – Logout/protected-route inconsistency in reused browser session
- Type: Exploratory observation; not a confirmed defect.
- Reproduction: Logout, then navigate to `/claims` and `/claims/new`; repeat in fresh Incognito context.
- Expected: Protected routes consistently redirect after logout.
- Actual: Reused session sometimes showed stale content/form; fresh Incognito redirected to `/login?redirect=%2Fclaims%2Fnew`.
- Status: Retest with clean session; do not claim a confirmed security bypass.

## API security observations
GET claims without bearer token: 401. POST without CSRF header: 403. POST with matching CSRF cookie/header but no bearer token: 401. These support expected enforcement and are not defects.
