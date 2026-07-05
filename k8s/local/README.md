# Local Kubernetes Runbook

This runs Chess Lab as local Kubernetes services:

```text
Vue frontend -> Spring Boot API -> RabbitMQ -> analysis worker -> Stockfish
                         |
                         v
                      Postgres
                                      ^-----------------------------|
```

The manifests use local image tags and do not require a registry.

## Build Images

From the repo root:

```powershell
docker build -t chess-lab-api:local .\api-service
docker build -t chess-lab-worker:local .\analysis-worker
docker build -t chess-lab-frontend:local .\frontend
```

If you use `kind`, load the images into the cluster:

```powershell
kind create cluster --name chess-lab
kind load docker-image chess-lab-api:local --name chess-lab
kind load docker-image chess-lab-worker:local --name chess-lab
kind load docker-image chess-lab-frontend:local --name chess-lab
```

Docker Desktop Kubernetes can usually use the local Docker images directly.

## Apply

```powershell
kubectl apply -f .\k8s\local\namespace.yaml
kubectl apply -f .\k8s\local
kubectl -n chess-lab get pods
```

Wait until `rabbitmq`, `api-service`, `analysis-worker`, and `frontend` are ready.
Postgres should also be ready; it stores imported games, analysis jobs, and reports.

## Open The App

Run these in separate terminals:

```powershell
kubectl -n chess-lab port-forward svc/api-service 8080:8080
kubectl -n chess-lab port-forward svc/frontend 5175:80
```

Then open:

```text
http://127.0.0.1:5175
```

The frontend build calls the API at `http://127.0.0.1:8080`, so keep the API port-forward running.

If port `8080` is already in use, choose another local API port and rebuild the frontend with that API URL:

```powershell
docker build --build-arg VITE_API_BASE_URL=http://127.0.0.1:18080 -t chess-lab-frontend:local .\frontend
kind load docker-image chess-lab-frontend:local --name chess-lab
kubectl -n chess-lab rollout restart deployment/frontend
kubectl -n chess-lab port-forward svc/api-service 18080:8080
kubectl -n chess-lab port-forward svc/frontend 15175:80
```

Optional RabbitMQ management UI:

```powershell
kubectl -n chess-lab port-forward svc/rabbitmq 15672:15672
```

Open `http://127.0.0.1:15672` and sign in with `guest` / `guest`.

## Inspect

```powershell
kubectl -n chess-lab get deploy,svc,pods
kubectl -n chess-lab logs deploy/api-service -f
kubectl -n chess-lab logs deploy/analysis-worker -f
kubectl -n chess-lab describe pod -l app=analysis-worker
```

The API publishes analysis requests to `chesslab.analysis.requests`. The worker consumes those requests, runs Stockfish, and publishes completed reports to `chesslab.analysis.results`.

## Cleanup

```powershell
kubectl delete -f .\k8s\local
```
