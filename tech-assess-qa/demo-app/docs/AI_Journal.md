# AI Working Journal

## Verbatim prompt limitation
The assessment email requires every prompt to be copied exactly. This file is not a complete verbatim transcript of every earlier prompt in the conversation. It records collaboration decisions available from the work history. Append/export the full ChatGPT transcript before claiming strict prompt-by-prompt completeness.

## Collaboration decisions
| Area | AI-assisted action | Decision | Reason |
|---|---|---|---|
| BFF login | Narrow auto-provisioning to `UserNotFoundException`; distinguish downstream 404 from generic failures | Accepted | Avoid creating users on unrelated downstream errors |
| BFF unit tests | Add 8 tests across login use case and adapter | Accepted; 8 passed | Verify provisioning, retry and error mapping |
| Claims Service | Add 5 create-claim tests | Accepted; 5 passed | Cover domain flow, CDC mode and metrics |
| UI | Add 12 Vitest/Testing Library tests | Accepted; 12 passed | User-facing component/schema behaviour |
| Playwright | POM, fixture and six login/navigation tests | Accepted; 6 passed | Reusable setup and clear assertions |
| Browser setup | Use installed Chrome after browser download stalled | Accepted | Reduce setup delay |
| Video | Do not claim video; FFmpeg unavailable | Constraint disclosed | Evidence honesty |
| API 403 | Investigate CSRF before classifying as defect; retry with CSRF header | Accepted; result 401 | Separate CSRF rejection from authentication rejection |
| Logout | Treat inconsistent reused-session observation as unconfirmed | Accepted | Insufficient reproducibility for security defect |

## Prompt-log maintenance
Append each new user prompt verbatim and note accepted/challenged/overridden AI suggestions. Never include credentials, tokens or secrets.
