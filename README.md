# Chess Lab

Local-first chess training app built with Vue, Spring Boot, and Stockfish.

Chess Lab imports chess games, keeps a local study library, and is structured to run Stockfish analysis through a worker service. The product direction is deliberately focused: no AI summaries, no accounts, no cloud sync, and no scraping from chess sites.

## Stack

- Frontend: Vue 3, Vite, TypeScript, Vue Router, Pinia, chess.js
- API: Spring Boot 4.1, Java 21
- Worker: Spring Boot 4.1, Java 21, Stockfish boundary
- Queue boundary: RabbitMQ
- Local runtime: Docker Compose

## Layout

```text
frontend/          Vue client
api-service/       Spring Boot REST API
analysis-worker/   Spring Boot Stockfish worker
docs/              architecture and runbooks
```

## Local Prerequisites

Install these for the full local runtime:

- Node.js 20+
- Java 21
- Maven or the Maven Wrapper generated inside each Spring Boot service
- Docker Desktop
- Stockfish for native worker runs; the worker container installs Stockfish

When this scaffold was created, Node was available locally, but Java, Maven, Docker, and Stockfish were not on PATH, so backend and compose verification were not run on this machine.

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
.\mvnw.cmd spring-boot:run
```

Analysis worker:

```powershell
cd analysis-worker
$env:STOCKFISH_PATH='stockfish'
.\mvnw.cmd spring-boot:run
```

Docker Compose, once Docker is installed:

```powershell
docker compose up --build
```

The frontend defaults to `http://127.0.0.1:8080` for API calls. Set `VITE_API_BASE_URL` before building if you run the API elsewhere.

## Current Slice

- PGN import is parsed client-side with `chess.js`, including legal final FEN and move table extraction.
- The API stores imported games in memory and exposes list/detail/analysis endpoints.
- Analysis jobs currently return `queued` until API job publishing and worker result writes are wired end to end.
- The worker contains deterministic move classification rules and a UCI Stockfish process client; it does not fabricate engine analysis.

## Next Engineering Steps

1. Wire API job publishing to RabbitMQ and worker consumption.
2. Persist Stockfish evaluations and expose completed reports through the API.
3. Add durable storage for games, jobs, and reports.
4. Add mistake replay drills once engine reports are available.
