import { Chess } from "chess.js";

import type { MoveRecord } from "../types";

export interface PgnPreview {
  black: string;
  finalFen: string;
  moves: MoveRecord[];
  plyCount: number;
  result: string;
  white: string;
}

export function previewPgn(pgn: string): PgnPreview {
  const chess = new Chess();

  try {
    chess.loadPgn(pgn, { strict: false });
  } catch (error) {
    throw new Error("Could not parse PGN. Check that the game text is complete and legal.", { cause: error });
  }

  const headers = chess.getHeaders();
  const history = chess.history();
  if (history.length === 0) {
    throw new Error("Could not parse PGN. Check that the game text is complete and legal.");
  }

  return {
    black: headers.Black ?? "Unknown",
    finalFen: chess.fen(),
    moves: toMoveRecords(history),
    plyCount: history.length,
    result: headers.Result ?? "*",
    white: headers.White ?? "Unknown"
  };
}

function toMoveRecords(history: string[]): MoveRecord[] {
  const records: MoveRecord[] = [];

  for (let index = 0; index < history.length; index += 2) {
    records.push({
      blackMove: history[index + 1],
      fullMoveNumber: index / 2 + 1,
      whiteMove: history[index]
    });
  }

  return records;
}
