import { Chess } from "chess.js";
import { describe, expect, it } from "vitest";

import { buildManualGamePayload, inferManualResult } from "./manualGame";

describe("manual game import", () => {
  it("builds the same import payload shape as PGN import", () => {
    const chess = new Chess();
    chess.move("e4");
    chess.move("e5");

    const payload = buildManualGamePayload(chess, {
      black: "Manual Black",
      result: "*",
      white: "Manual White"
    });

    expect(payload.white).toBe("Manual White");
    expect(payload.black).toBe("Manual Black");
    expect(payload.result).toBe("*");
    expect(payload.plyCount).toBe(2);
    expect(payload.pgn).toContain('[White "Manual White"]');
    expect(payload.moves[0]).toEqual({
      blackFenBefore: "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
      blackMove: "e5",
      blackUci: "e7e5",
      fullMoveNumber: 1,
      whiteFenBefore: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
      whiteMove: "e4",
      whiteUci: "e2e4"
    });
  });

  it("infers completed game results when manual entry reaches mate", () => {
    const chess = new Chess();
    ["e4", "e5", "Bc4", "Nc6", "Qh5", "Nf6", "Qxf7#"].forEach((move) => chess.move(move));

    expect(inferManualResult(chess)).toBe("1-0");
  });
});
