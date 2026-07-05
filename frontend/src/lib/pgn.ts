import { Chess, type Move } from "chess.js";

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
  const history = chess.history({ verbose: true });
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

function toMoveRecords(history: Move[]): MoveRecord[] {
  const records: MoveRecord[] = [];

  for (let index = 0; index < history.length; index += 2) {
    const whiteMove = history[index];
    const blackMove = history[index + 1];
    records.push({
      blackFenBefore: blackMove?.before,
      blackMove: blackMove?.san,
      blackUci: blackMove?.lan,
      fullMoveNumber: index / 2 + 1,
      whiteFenBefore: whiteMove.before,
      whiteMove: whiteMove.san,
      whiteUci: whiteMove.lan
    });
  }

  return records;
}
