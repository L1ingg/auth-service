# Auth Service

Authentication service for ConnectMe.

Provides:

* User registration
* Authentication (JWT)
* Access token refresh
* Email verification

## Run

Start only the auth service:

```bash
docker compose up --build auth-service
```

Build locally:

```bash
./gradlew build
docker build -t auth-service:local .
```

## Port

| Host | Container |
| ---- | --------- |
| 9090 | 8080      |

## API

### Register

```http
POST /api/auth/register
```

Request:

```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

Responses:

* `200 OK`
* `409 CONFLICT`

### Login

```http
POST /api/auth/login
```

Request:

```json
{
  "email": "string",
  "password": "string"
}
```

Response:

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>"
}
```

### Refresh Token

```http
POST /api/auth/refresh
```

Request:

```json
{
  "refreshToken": "<jwt>"
}
```

Response:

```json
{
  "accessToken": "<jwt>"
}
```

### Verify Email

```http
POST /api/auth/email/verify?code=<code>
```

Responses:

* `200 OK`
* `404 NOT FOUND`
* `409 CONFLICT`

## Authentication

Protected endpoints require:

```http
Authorization: Bearer <accessToken>
```

