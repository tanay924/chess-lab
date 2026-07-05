import { Chess } from "chess.js";

import type { GameImportPayload } from "../types";
import { toMoveRecords } from "./pgn";

interface ManualGameMetadata {
  black: string;
  result?: string;
  white: string;
}

export function buildManualGamePayload(chess: Chess, metadata: ManualGameMetadata): GameImportPayload {
  const history = chess.history({ verbose: true });
  const result = metadata.result ?? inferManualResult(chess);

  chess.setHeader("White", metadata.white);
  chess.setHeader("Black", metadata.black);
  chess.setHeader("Result", result);

  return {
    black: metadata.black,
    finalFen: chess.fen(),
    moves: toMoveRecords(history),
    pgn: chess.pgn(),
    plyCount: history.length,
    result,
    white: metadata.white
  };
}

export function inferManualResult(chess: Chess): string {
  if (chess.isCheckmate()) {
    return chess.turn() === "w" ? "0-1" : "1-0";
  }
  if (chess.isDraw()) {
    return "1/2-1/2";
  }
  return "*";
}
