import { describe, expect, it } from "vitest";

import { previewPgn } from "./pgn";

const scholarMate = `[Event "Casual Game"]
[White "White"]
[Black "Black"]
[Result "1-0"]

1. e4 e5 2. Bc4 Nc6 3. Qh5 Nf6 4. Qxf7# 1-0`;

describe("previewPgn", () => {
  it("extracts headers, move count, result, and final FEN", () => {
    const preview = previewPgn(scholarMate);

    expect(preview.white).toBe("White");
    expect(preview.black).toBe("Black");
    expect(preview.result).toBe("1-0");
    expect(preview.plyCount).toBe(7);
    expect(preview.finalFen).toContain("r1bqkb1r/pppp1Qpp");
    expect(preview.moves[0]).toEqual({ blackMove: "e5", fullMoveNumber: 1, whiteMove: "e4" });
    expect(preview.moves[3]).toEqual({ fullMoveNumber: 4, whiteMove: "Qxf7#" });
  });

  it("returns a readable error for invalid PGN", () => {
    expect(() => previewPgn("not a pgn")).toThrow("Could not parse PGN");
  });
});
