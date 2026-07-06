# User Auth And Private Libraries Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Spring Security session authentication, private per-user game libraries, and simple queued FIFO analysis status.

**Architecture:** The API owns authentication and game authorization through Spring Security sessions and JPA users. Vue stores the current session, includes cookies and CSRF tokens on API calls, and protects app routes. RabbitMQ remains the FIFO analysis queue; jobs are created as `QUEUED` and workers publish `RUNNING` then terminal status.

**Tech Stack:** Spring Boot 4.1, Spring Security, Spring Data JPA, PostgreSQL/H2, RabbitMQ, Vue 3, Pinia, Vue Router, Vitest.

---

### Task 1: Backend Security And Auth Domain

**Files:**
- Modify: `api-service/pom.xml`
- Create: `api-service/src/main/java/com/tanay/chesslab/api/auth/AppUser.java`
- Create: `api-service/src/main/java/com/tanay/chesslab/api/auth/AppUserRepository.java`
- Create: `api-service/src/main/java/com/tanay/chesslab/api/auth/AuthService.java`
- Create: `api-service/src/main/java/com/tanay/chesslab/api/auth/AuthController.java`
- Create: `api-service/src/main/java/com/tanay/chesslab/api/auth/AuthDtos.java`
- Create: `api-service/src/main/java/com/tanay/chesslab/api/config/SecurityConfig.java`
- Modify: `api-service/src/main/java/com/tanay/chesslab/api/config/CorsConfig.java`
- Test: `api-service/src/test/java/com/tanay/chesslab/api/auth/AuthServiceTest.java`

- [ ] Add Spring Security dependencies and tests.
- [ ] Write failing tests for registration, username normalization, password hashing, and duplicate rejection.
- [ ] Implement `AppUser`, repository, `AuthService`, and DTOs.
- [ ] Add `SecurityConfig` with session auth, BCrypt, CSRF token cookie, JSON 401/403, and credentialed CORS.
- [ ] Add auth controller endpoints for session, csrf, register, login, logout.
- [ ] Run API auth tests.

### Task 2: Owner-Scoped Games

**Files:**
- Modify: `api-service/src/main/java/com/tanay/chesslab/api/persistence/JpaGameEntity.java`
- Modify: `api-service/src/main/java/com/tanay/chesslab/api/persistence/JpaGameRepository.java`
- Modify: `api-service/src/main/java/com/tanay/chesslab/api/persistence/JpaGameStore.java`
- Modify: `api-service/src/main/java/com/tanay/chesslab/api/persistence/GameStore.java`
- Modify: `api-service/src/main/java/com/tanay/chesslab/api/persistence/InMemoryGameStore.java`
- Modify: `api-service/src/main/java/com/tanay/chesslab/api/service/GameService.java`
- Modify: `api-service/src/main/java/com/tanay/chesslab/api/web/GameController.java`
- Test: `api-service/src/test/java/com/tanay/chesslab/api/service/GameServiceTest.java`

- [ ] Write failing service tests proving user B cannot list or fetch user A's games.
- [ ] Add `ownerUsername` or `ownerId` ownership to store APIs.
- [ ] Persist game owner and query by owner.
- [ ] Scope create/list/get/report/start-analysis by authenticated username.
- [ ] Make inaccessible games return not found.
- [ ] Run API service tests.

### Task 3: Queued FIFO Status

**Files:**
- Modify: `api-service/src/main/java/com/tanay/chesslab/api/domain/AnalysisStatus.java`
- Modify: `api-service/src/main/java/com/tanay/chesslab/api/service/GameService.java`
- Modify: `analysis-worker/src/main/java/com/tanay/chesslab/worker/messaging/WorkerAnalysisListener.java`
- Test: `api-service/src/test/java/com/tanay/chesslab/api/service/GameServiceTest.java`
- Test: `analysis-worker/src/test/java/com/tanay/chesslab/worker/messaging/AnalysisRequestListenerTest.java`

- [ ] Write failing tests for queued job creation and worker running publication.
- [ ] Create jobs as `QUEUED` before dispatch.
- [ ] Save queued report message before dispatch.
- [ ] Worker publishes `RUNNING` when it starts processing and then publishes final report.
- [ ] Run API and worker tests.

### Task 4: Frontend Auth

**Files:**
- Modify: `frontend/src/api.ts`
- Modify: `frontend/src/types.ts`
- Create: `frontend/src/stores/auth.ts`
- Create: `frontend/src/pages/LoginPage.vue`
- Create: `frontend/src/pages/RegisterPage.vue`
- Modify: `frontend/src/router.ts`
- Modify: `frontend/src/App.vue`
- Modify: `frontend/src/style.css`
- Test: `frontend/src/api.test.ts`

- [ ] Write failing frontend tests that mutating requests include credentials and CSRF token.
- [ ] Add auth API types and helpers.
- [ ] Add credentials and CSRF handling to all API requests.
- [ ] Add Pinia auth store.
- [ ] Add login/register pages.
- [ ] Add route guard for protected routes.
- [ ] Add topbar current-user and logout controls.
- [ ] Run frontend tests and build.

### Task 5: Integration Verification

**Files:**
- Modify: `README.md`
- Modify: `k8s/local/README.md`

- [ ] Update docs with registration/login, private libraries, and session behavior.
- [ ] Run `npm run test` and `npm run build` in `frontend`.
- [ ] Run API tests in Docker Maven.
- [ ] Run worker tests in Docker Maven.
- [ ] Rebuild API, worker if needed, and frontend images.
- [ ] Load changed images into kind and restart deployments.
- [ ] Verify register/login/import/library isolation manually against `http://127.0.0.1:15175`.
- [ ] Commit and push.
