# Chess Lab

Local-first chess training app built with Vue, Spring Boot, and Stockfish.

Chess Lab imports chess games, keeps a local study library, and runs Stockfish analysis on your machine. The product direction is deliberately focused: no AI summaries, no accounts, no cloud sync, and no scraping from chess sites.

## Stack

- Frontend: Vue 3, Vite, TypeScript, Vue Router, Pinia, chess.js
- API: Spring Boot 4.1, Java 21, RabbitMQ producer/result consumer
- Worker: Spring Boot 4.1, Java 21, RabbitMQ consumer, Stockfish runner
- Queue: RabbitMQ
- Persistence: Spring Data JPA with Postgres in Compose/k8s and H2 fallback for standalone local runs
- Optional local runtime: Docker Compose
- Optional local orchestration: Kubernetes with Docker Desktop or kind

## Layout

```text
frontend/          Vue client
api-service/       Spring Boot REST API
analysis-worker/   Spring Boot Stockfish worker
docs/              architecture and runbooks
k8s/local/         local Kubernetes manifests
```

## Local Prerequisites

Install these for the normal local runtime:

- Node.js 20+
- Java 21
- Maven or the Maven Wrapper generated inside each Spring Boot service
- Stockfish 18 or another UCI-compatible Stockfish binary
- RabbitMQ for queued analysis

Docker Desktop is only needed if you want to run the app with Compose or local Kubernetes.

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
$env:SPRING_RABBITMQ_HOST='127.0.0.1'
.\mvnw.cmd spring-boot:run
```

Worker service:

```powershell
cd analysis-worker
$env:SPRING_RABBITMQ_HOST='127.0.0.1'
$env:STOCKFISH_PATH='C:\path\to\stockfish.exe'
.\mvnw.cmd spring-boot:run
```

Optional tuning:

```powershell
$env:STOCKFISH_DEPTH='8'
$env:ANALYSIS_MAX_PLIES='80'
```

Analysis jobs are queued through RabbitMQ. The API creates jobs and serves reports; the worker consumes jobs, runs Stockfish, and publishes completed reports back to the API.

When the API is run directly without database environment variables, it stores games and reports in `api-service/data/chesslab.mv.db`. Compose and local Kubernetes use Postgres instead.

Docker Compose, once Docker is installed:

```powershell
docker compose up --build
```

Compose exposes:

- Frontend: `http://127.0.0.1:5175`
- API: `http://127.0.0.1:8080`
- Postgres: `127.0.0.1:5433` (`chesslab` / `chesslab`)
- RabbitMQ management: `http://127.0.0.1:15673` (`guest` / `guest`)

Local Kubernetes:

```powershell
docker build -t chess-lab-api:local .\api-service
docker build -t chess-lab-worker:local .\analysis-worker
docker build -t chess-lab-frontend:local .\frontend
kubectl apply -f .\k8s\local\namespace.yaml
kubectl apply -f .\k8s\local
kubectl -n chess-lab get pods
```

See `k8s/local/README.md` for port-forwarding, kind image loading, logs, and cleanup.

The frontend defaults to `http://127.0.0.1:8080` for API calls. Set `VITE_API_BASE_URL` before building if you run the API elsewhere.

## Current Slice

- PGN import and manual starting-position entry are parsed client-side with `chess.js`, including legal final FEN and move table extraction.
- The API persists imported games, analysis jobs, and reports through JPA, and exposes list/detail/analysis endpoints.
- Analysis jobs run asynchronously through RabbitMQ; the worker calls local Stockfish for each ply.
- The frontend polls analysis reports and renders the engine's best move plus a first-pass classification.
- The worker contains deterministic move classification rules and a UCI Stockfish process client; it does not fabricate engine analysis.

## Next Engineering Steps

1. Replace the first-pass best-move-only classification with centipawn-loss based grading.
2. Add mistake replay drills from completed engine reports.
3. Add optional observability dashboards around API latency, queue depth, and worker analysis time.
