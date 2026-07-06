import type {
  AnalysisJob,
  AnalysisReport,
  AuthCredentials,
  AuthSession,
  CsrfToken,
  GameDetail,
  GameImportPayload,
  GameSummary
} from "./types";

const API_BASE = resolveApiBaseUrl(import.meta.env.VITE_API_BASE_URL);
let cachedCsrfToken: CsrfToken | null = null;

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
  const response = await apiFetch("/api/games", {
    body: JSON.stringify(payload),
    method: "POST"
  });
  return parseResponse<GameDetail>(response);
}

export async function listGames(): Promise<GameSummary[]> {
  const response = await apiFetch("/api/games");
  return parseResponse<GameSummary[]>(response);
}

export async function getGame(gameId: string): Promise<GameDetail> {
  const response = await apiFetch(`/api/games/${gameId}`);
  return parseResponse<GameDetail>(response);
}

export async function startAnalysis(gameId: string): Promise<AnalysisJob> {
  const response = await apiFetch(`/api/games/${gameId}/analysis`, { method: "POST" });
  return parseResponse<AnalysisJob>(response);
}

export async function getReport(gameId: string): Promise<AnalysisReport> {
  const response = await apiFetch(`/api/games/${gameId}/analysis-report`);
  return parseResponse<AnalysisReport>(response);
}

export async function getSession(): Promise<AuthSession> {
  const response = await apiFetch("/api/auth/session");
  return parseResponse<AuthSession>(response);
}

export async function login(credentials: AuthCredentials): Promise<AuthSession> {
  const response = await apiFetch("/api/auth/login", {
    body: JSON.stringify(credentials),
    method: "POST"
  });
  return parseResponse<AuthSession>(response);
}

export async function register(credentials: AuthCredentials): Promise<AuthSession> {
  const response = await apiFetch("/api/auth/register", {
    body: JSON.stringify(credentials),
    method: "POST"
  });
  return parseResponse<AuthSession>(response);
}

export async function logout(): Promise<AuthSession> {
  const response = await apiFetch("/api/auth/logout", { method: "POST" });
  cachedCsrfToken = null;
  return parseResponse<AuthSession>(response);
}

export async function apiFetch(path: string, init: RequestInit = {}): Promise<Response> {
  return fetch(`${API_BASE}${path}`, await buildRequestInit(init));
}

export async function buildRequestInit(init: RequestInit = {}): Promise<RequestInit> {
  const method = (init.method ?? "GET").toUpperCase();
  const headers = new Headers(init.headers);
  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
    const token = await getCsrfToken();
    headers.set(token.headerName, token.token);
  }
  return {
    ...init,
    credentials: "include",
    headers
  };
}

async function getCsrfToken(): Promise<CsrfToken> {
  if (cachedCsrfToken) {
    return cachedCsrfToken;
  }
  const response = await fetch(`${API_BASE}/api/auth/csrf`, { credentials: "include" });
  cachedCsrfToken = await parseResponse<CsrfToken>(response);
  return cachedCsrfToken;
}

async function parseResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed with ${response.status}`);
  }
  return response.json() as Promise<T>;
}
