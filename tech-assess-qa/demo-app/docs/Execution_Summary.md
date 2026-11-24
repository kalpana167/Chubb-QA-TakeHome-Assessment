# Execution Summary

| Layer | Result |
|---|---|
| BFF Maven tests | 8 passed |
| Claims Service Maven tests | 5 passed; Checkstyle 0 violations |
| UI Vitest | 12 passed |
| Playwright | 6 passed |
| ESLint | 0 errors, 8 warnings |
| UI build | Successful |

Manual UI: signup/login; too-short description rejected; valid claim submitted; claim details matched submitted values; fresh Incognito `/claims/new` redirected to login. Logout in a reused session was inconsistent and needs retest.

Claim submitted: ID `2e6adfe3-1d76-48f3-aaef-d4b0eeba317b`; date 2026-11-24; location `123 test`; amount `$100.00`; status Submitted.

API exploratory checks: API_TC_001 GET claims without bearer token → 401; API_TC_002 invalid login → 401; API_TC_003 empty login body → 400; API_TC_004 POST claims with CSRF cookie/header but no bearer token → 401; API_TC_005 POST claims without CSRF header → 403.

Limitations: curl checks are not a complete automated API schema suite; authenticated claim-field validation is outstanding. Playwright video was not generated because FFmpeg was unavailable. Screenshots must be copied from local `test-results` into the repository evidence folder before claiming attachment.
