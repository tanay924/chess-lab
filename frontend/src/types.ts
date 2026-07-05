export type AnalysisStatus = "queued" | "running" | "ready" | "failed";

export interface GameSummary {
  black: string;
  createdAt: string;
  id: string;
  plyCount: number;
  result: string;
  white: string;
}

export interface GameDetail extends GameSummary {
  finalFen: string;
  moves: MoveRecord[];
  pgn: string;
}

export interface GameImportPayload {
  black: string;
  finalFen: string;
  moves: MoveRecord[];
  pgn: string;
  plyCount: number;
  result: string;
  white: string;
}

export interface MoveRecord {
  blackFenBefore?: string;
  blackMove?: string;
  blackUci?: string;
  fullMoveNumber: number;
  whiteFenBefore: string;
  whiteMove: string;
  whiteUci: string;
}

export interface AnalysisJob {
  gameId: string;
  id: string;
  status: AnalysisStatus;
}

export interface AnalysisReport {
  evaluations: MoveEvaluation[];
  gameId: string;
  jobId: string;
  message?: string;
  status: AnalysisStatus;
}

export interface MoveEvaluation {
  bestMove: string;
  classification: "excellent" | "good" | "inaccuracy" | "mistake" | "blunder";
  playedMove: string;
  ply: number;
  scoreCp: number;
}
