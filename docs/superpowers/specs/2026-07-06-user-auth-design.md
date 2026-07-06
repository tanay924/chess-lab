# User Auth And Private Libraries Design

## Summary

Add proper Spring Security session authentication to Chess Lab. Anyone can register with a username and password, registration logs the user in, and authenticated users only see and access their own games, analysis jobs, and reports. Analysis remains worker-backed through RabbitMQ and uses a simple FIFO user experience: jobs are `queued`, then `running`, then `ready` or `failed`.

## Decisions

- Login is required for all meaningful app routes and API routes.
- Public API routes are limited to registration, login, logout, current session, CSRF token retrieval, and health endpoints.
- Auth uses Spring Security with server-side sessions and HTTP-only cookies.
- Sessions should survive browser restart for roughly seven days.
- Passwords are hashed with BCrypt.
- Usernames are case-insensitive unique. The original display casing can be kept.
- Password rule is minimum eight characters, no complexity rules.
- Existing test games do not need to be preserved. After ownership is added, unowned rows may be deleted or made inaccessible.
- Other users' games are not accessible. Returning `404 Not Found` for inaccessible games is acceptable.
- CSRF is handled properly for cookie-auth mutating requests.

## Backend Design

Add a `users` table with username display value, normalized username, password hash, and creation timestamp. Add ownership to games via `owner_id`. The service layer receives the authenticated user identity and scopes create, list, get, report, and analysis-start operations by user.

Add Spring Security configuration:

- Permit `/actuator/health`, `/actuator/info`, and `/api/auth/**`.
- Require authentication for `/api/**`.
- Use BCrypt password encoder.
- Use CSRF protection with a cookie-readable token so the Vue app can send `X-CSRF-TOKEN`.
- Use CORS with credentials allowed for local frontend origins.
- Return JSON-friendly `401`/`403` responses rather than redirecting to a login page.

Add auth endpoints:

- `GET /api/auth/session`: returns current user or anonymous.
- `GET /api/auth/csrf`: returns the CSRF token.
- `POST /api/auth/register`: creates user and logs in.
- `POST /api/auth/login`: logs in.
- `POST /api/auth/logout`: logs out.

For analysis FIFO, persist jobs as `QUEUED` first. The worker already consumes one RabbitMQ queue; with one worker pod this gives FIFO processing. The worker should publish a `RUNNING` result/status when it starts a job so the UI can distinguish queued from active work.

## Frontend Design

Add auth API helpers and an auth store. Configure all API requests with `credentials: "include"` and attach `X-CSRF-TOKEN` for mutating methods.

Add `/login` and `/register` pages. Login and register remain public. Import, library, and game review routes require a current authenticated user. The top bar shows the current username and a Logout button when logged in.

When logged out, route guards redirect protected routes to `/login`. After login/register, users land on `/library` or return to their intended route.

## Testing

Backend tests cover:

- Registration hashes passwords and enforces case-insensitive uniqueness.
- Login accepts valid credentials and rejects invalid credentials.
- Game listing only returns the current user's games.
- Direct access to another user's game returns not found.
- Starting analysis creates a queued job and dispatches it.
- Worker marks jobs running before completion.
- CORS allows credentials from local frontend origins.

Frontend tests cover:

- API requests include credentials.
- Mutating API requests attach CSRF tokens.
- Auth route guard blocks protected routes when anonymous.

Manual verification covers:

- Register user A, import a game, see it in A's library.
- Logout, register user B, verify B's library does not show A's game.
- Direct URL to A's game while logged in as B is inaccessible.
- Run analysis and see queued/running/ready flow.
