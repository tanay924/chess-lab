/// <reference types="node" />

import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";

import { describe, expect, it } from "vitest";

describe("ChessBoard styles", () => {
  it("locks the board to an even 8 by 8 grid", () => {
    const css = readFileSync(fileURLToPath(new URL("../style.css", import.meta.url)), "utf8");

    expect(css).toContain("grid-template-columns: repeat(8, 1fr);");
    expect(css).toContain("grid-template-rows: repeat(8, 1fr);");
  });
});
