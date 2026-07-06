import { describe, expect, it } from "vitest";

import { resolveApiBaseUrl } from "./api";

describe("resolveApiBaseUrl", () => {
  it("maps the local k8s frontend port to the local k8s API port when no explicit URL is set", () => {
    expect(resolveApiBaseUrl(undefined, "http://127.0.0.1:15175/")).toBe("http://127.0.0.1:18080");
  });
});
