import { Chess } from "chess.js";

import type { GameDetail } from "../types";

export interface ReplayPosition {
  fen: string;
  label: string;
  move?: string;
  ply: number;
}

export function buildReplayPositions(game: GameDetail): ReplayPosition[] {
  const startFen = game.moves[0]?.whiteFenBefore;
  const chess = startFen ? new Chess(startFen) : new Chess();
  const positions: ReplayPosition[] = [{ fen: chess.fen(), label: "Start", ply: 0 }];

  for (const move of game.moves) {
    pushMovePosition(positions, chess, move.whiteUci, move.whiteMove, `${move.fullMoveNumber}. ${move.whiteMove}`);
    if (move.blackMove && move.blackUci) {
      pushMovePosition(positions, chess, move.blackUci, move.blackMove, `${move.fullMoveNumber}... ${move.blackMove}`);
    }
  }

  return positions;
}

function pushMovePosition(
  positions: ReplayPosition[],
  chess: Chess,
  uci: string | undefined,
  san: string | undefined,
  label: string
) {
  if (!uci || !san) {
    return;
  }

  const move = chess.move({
    from: uci.slice(0, 2),
    promotion: uci.length > 4 ? uci.slice(4, 5) : undefined,
    to: uci.slice(2, 4)
  });

  positions.push({
    fen: chess.fen(),
    label,
    move: move.san,
    ply: positions.length
  });
}
