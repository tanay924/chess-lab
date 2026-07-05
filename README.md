# Chess Lab

Local-first chess training app built with Vue, Spring Boot, and Stockfish.

Chess Lab imports chess games, keeps a local study library, and runs Stockfish analysis on your machine. The product direction is deliberately focused: no AI summaries, no accounts, no cloud sync, and no scraping from chess sites.

## Stack

- Frontend: Vue 3, Vite, TypeScript, Vue Router, Pinia, chess.js
- API: Spring Boot 4.1, Java 21
- Worker: Spring Boot 4.1, Java 21, Stockfish boundary for the future queued architecture
- Optional queue boundary: RabbitMQ
- Optional local runtime: Docker Compose

## Layout

```text
frontend/          Vue client
api-service/       Spring Boot REST API
analysis-worker/   Spring Boot Stockfish worker
docs/              architecture and runbooks
```

## Local Prerequisites

Install these for the normal local runtime:

- Node.js 20+
- Java 21
- Maven or the Maven Wrapper generated inside each Spring Boot service
- Stockfish 18 or another UCI-compatible Stockfish binary

Docker Desktop is only needed if you want to run the future worker/queue topology with Compose. The direct local API analysis flow does not require Docker.

## Run Locally

Frontend only:

```powershell
cd frontend
npm install
npm run dev
```

API service:

```powershell
cd api-service
$env:STOCKFISH_PATH='C:\path\to\stockfish.exe'
.\mvnw.cmd spring-boot:run
```

Optional tuning:

```powershell
$env:STOCKFISH_DEPTH='8'
$env:ANALYSIS_MAX_PLIES='80'
```

Analysis is processed inside the API service for the current local-first slice. The separate `analysis-worker` module remains in the repo for the later RabbitMQ-backed architecture.

Docker Compose, once Docker is installed:

```powershell
docker compose up --build
```

The frontend defaults to `http://127.0.0.1:8080` for API calls. Set `VITE_API_BASE_URL` before building if you run the API elsewhere.

## Current Slice

- PGN import and manual starting-position entry are parsed client-side with `chess.js`, including legal final FEN and move table extraction.
- The API stores imported games in memory and exposes list/detail/analysis endpoints.
- Analysis jobs run asynchronously in the API service and call local Stockfish for each ply.
- The frontend polls analysis reports and renders the engine's best move plus a first-pass classification.
- The worker contains deterministic move classification rules and a UCI Stockfish process client for the future queued service; it does not fabricate engine analysis.

## Next Engineering Steps

1. Replace the first-pass best-move-only classification with centipawn-loss based grading.
2. Persist games, jobs, and reports so imported games survive API restarts.
3. Add mistake replay drills from completed engine reports.
4. Optionally move analysis execution behind RabbitMQ and the worker service once durable jobs matter.
