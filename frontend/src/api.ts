import type { AnalysisJob, AnalysisReport, GameDetail, GameImportPayload, GameSummary } from "./types";

const API_BASE = resolveApiBaseUrl(import.meta.env.VITE_API_BASE_URL);

export function resolveApiBaseUrl(explicitUrl?: string, pageHref?: string): string {
  if (explicitUrl?.trim()) {
    return explicitUrl;
  }

  const href = pageHref ?? (typeof window === "undefined" ? "http://127.0.0.1:5173/" : window.location.href);
  const pageUrl = new URL(href);
  const apiPortByFrontendPort: Record<string, string> = {
    "5173": "8080",
    "5174": "8080",
    "5175": "8080",
    "15175": "18080"
  };
  const apiPort = apiPortByFrontendPort[pageUrl.port] ?? "8080";

  return `${pageUrl.protocol}//${pageUrl.hostname}:${apiPort}`;
}

export async function createGame(payload: GameImportPayload): Promise<GameDetail> {
  const response = await fetch(`${API_BASE}/api/games`, {
    body: JSON.stringify(payload),
    headers: { "Content-Type": "application/json" },
    method: "POST"
  });
  return parseResponse<GameDetail>(response);
}

export async function listGames(): Promise<GameSummary[]> {
  const response = await fetch(`${API_BASE}/api/games`);
  return parseResponse<GameSummary[]>(response);
}

export async function getGame(gameId: string): Promise<GameDetail> {
  const response = await fetch(`${API_BASE}/api/games/${gameId}`);
  return parseResponse<GameDetail>(response);
}

export async function startAnalysis(gameId: string): Promise<AnalysisJob> {
  const response = await fetch(`${API_BASE}/api/games/${gameId}/analysis`, { method: "POST" });
  return parseResponse<AnalysisJob>(response);
}

export async function getReport(gameId: string): Promise<AnalysisReport> {
  const response = await fetch(`${API_BASE}/api/games/${gameId}/analysis-report`);
  return parseResponse<AnalysisReport>(response);
}

async function parseResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed with ${response.status}`);
  }
  return response.json() as Promise<T>;
}
