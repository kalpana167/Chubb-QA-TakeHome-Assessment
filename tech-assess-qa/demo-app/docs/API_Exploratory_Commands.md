# API Exploratory Commands (Windows CMD)

```cmd
curl -i http://localhost:8090/api/claims
```
Observed 401 without bearer token.

```cmd
curl -i -X POST http://localhost:8090/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"invalid.qa@example.com\",\"password\":\"WrongPassword123\"}"
```
Observed 401 Authentication Failed.

```cmd
curl -i -X POST http://localhost:8090/api/auth/login -H "Content-Type: application/json" -d "{}"
```
Observed 400 Validation Error with missing email/password.

```cmd
curl -i -c cookies.txt http://localhost:8090/actuator/health
```
Observed 200 and XSRF-TOKEN cookie.

```cmd
curl -i -b cookies.txt -X POST http://localhost:8090/api/claims -H "Content-Type: application/json" -d "{}"
```
Observed 403 without CSRF header.

```cmd
curl -i -b cookies.txt -X POST http://localhost:8090/api/claims -H "Content-Type: application/json" -H "X-XSRF-TOKEN: <cookie-value>" -d "{}"
```
Observed 401 after CSRF validation because no bearer token was supplied. Never commit cookies, access tokens or secrets.
