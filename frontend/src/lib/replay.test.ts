import { describe, expect, it } from "vitest";

import type { GameDetail } from "../types";
import { buildReplayPositions } from "./replay";

describe("buildReplayPositions", () => {
  it("starts at move zero and advances through every ply", () => {
    const game: GameDetail = {
      black: "Black",
      createdAt: "2026-07-05T00:00:00.000Z",
      finalFen: "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2",
      id: "1",
      moves: [
        {
          blackFenBefore: "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
          blackMove: "e5",
          blackUci: "e7e5",
          fullMoveNumber: 1,
          whiteFenBefore: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
          whiteMove: "e4",
          whiteUci: "e2e4"
        }
      ],
      pgn: "1. e4 e5",
      plyCount: 2,
      result: "*",
      white: "White"
    };

    const positions = buildReplayPositions(game);

    expect(positions).toHaveLength(3);
    expect(positions[0]).toMatchObject({ label: "Start", ply: 0 });
    expect(positions[1]).toMatchObject({ label: "1. e4", move: "e4", ply: 1 });
    expect(positions[2]).toMatchObject({ label: "1... e5", move: "e5", ply: 2 });
    expect(positions[0].fen).toContain(" w KQkq - 0 1");
    expect(positions[2].fen).toBe(game.finalFen);
  });
});
