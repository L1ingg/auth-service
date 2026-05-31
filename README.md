# Auth Service

Description
- Authentication service for the ConnectMe system. Provides registration, login and token refresh endpoints.


Environment variables (see `docker-compose.yml`)
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` - Kafka bootstrap address (e.g. `broker:9092`)
- `SPRING_KAFKA_CONSUMER_GROUP_ID` - Kafka consumer group id
- `TOKEN_SIGNING_KEY` - Base64 signing key used for JWT generation
- `SPRING_DATASOURCE_URL` - JDBC URL for the database
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

Ports
- Exposed in docker-compose as `9090:8080` (host:container).

Usage / Endpoints

- POST /api/auth/register
	- Description: Register a new user.
	- Request JSON body: `{ "username": "string", "email": "string", "password": "string" }`
	- Responses: `200 OK` on success, `409 CONFLICT` if user already exists.

- POST /api/auth/login
	- Description: Authenticate and obtain tokens.
	- Request JSON body: `{ "email": "string", "password": "string" }`
	- Response (200): `{ "accessToken": "<jwt>", "refreshToken": "<jwt>" }`
	- Responses: `400 Bad Request` for invalid credentials or missing user.

- POST /api/auth/refresh
	- Description: Exchange a refresh token for a new access token.
	- Request JSON body: `{ "refreshToken": "<refresh-token>" }`
	- Response (200): `{ "accessToken": "<new-access-token>" }`
	- Responses: `400 Bad Request` for invalid or expired refresh token.

Authentication header
- Protected services in the system expect the access token in the `Authorization` header:

```
Authorization: Bearer <accessToken>
```

Examples
- Register:

```bash
curl -X POST http://localhost:9090/api/auth/register \
	-H "Content-Type: application/json" \
	-d '{"username":"alice","email":"alice@example.com","password":"p@ssw0rd"}'
```

- Login:

```bash
curl -X POST http://localhost:9090/api/auth/login \
	-H "Content-Type: application/json" \
	-d '{"email":"alice@example.com","password":"p@ssw0rd"}'
```

Notes
- Main application class: `com.ling.authService.AuthServiceApplication`.
- Sources: `src/main/java`, configuration: `src/main/resources`.
