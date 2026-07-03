# Chess Lab Initial Scaffold Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first local-first Chess Lab scaffold with Vue, Spring Boot API, Spring Boot worker, and local infrastructure.

**Architecture:** The frontend is a Vue 3 single-page app. The backend is split into an API service and a worker service so chess analysis can run asynchronously through a queue. V1 is local-first and Stockfish-only; Docker Compose is the intended runtime once Docker, Java, and Stockfish prerequisites are available.

**Tech Stack:** Vue 3, Vite, TypeScript, Pinia, Vue Router, Java 21, Spring Boot, RabbitMQ, Stockfish. PostgreSQL is planned after the worker loop is functional.

---

### Task 1: Repository Shell

**Files:**
- Create: `.gitignore`
- Create: `README.md`
- Create: `docker-compose.yml`
- Create: `docs/architecture.md`

- [x] Create root documentation that states the local-first scope, service boundaries, run commands, and known prerequisites.
- [x] Add Docker Compose services for RabbitMQ, API, worker, and frontend.
- [ ] Commit the repository shell.

### Task 2: Frontend Scaffold

**Files:**
- Create: `frontend/`
- Modify: `frontend/src/**`

- [x] Generate a Vue 3 + TypeScript + Vite app.
- [x] Add Vue Router and Pinia.
- [x] Build a first product UI with a PGN import page, game review page, analysis status panel, and report placeholders.
- [x] Add lightweight frontend tests for core formatting/parsing helpers.
- [x] Run `npm run build`.

### Task 3: Spring Boot API Service

**Files:**
- Create: `api-service/`

- [x] Generate a Java 21 Spring Boot Maven project.
- [x] Add domain records for games, moves, analysis jobs, and reports.
- [x] Add REST controllers for creating games, listing games, starting analysis, and reading analysis reports.
- [x] Use in-memory service implementations for the initial scaffold so the API can be understood before persistence is filled in.
- [x] Add unit tests for import storage and job lifecycle logic.

### Task 4: Spring Boot Worker Service

**Files:**
- Create: `analysis-worker/`

- [x] Generate a Java 21 Spring Boot Maven project.
- [x] Add a Stockfish UCI process client boundary.
- [x] Avoid fake engine output; reports stay queued until Stockfish is wired.
- [x] Add worker service classes for producing move evaluations from engine score comparisons.
- [x] Add unit tests around classification thresholds.

### Task 5: Final Verification

**Files:**
- Modify: `README.md`

- [x] Run available frontend verification on this machine.
- [x] Record backend verification limitations if Java, Maven, Docker, or Stockfish are missing.
- [ ] Commit the initial scaffold.
