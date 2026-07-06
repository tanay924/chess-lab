import { afterEach, describe, expect, it, vi } from "vitest";

import { buildRequestInit, resolveApiBaseUrl } from "./api";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("resolveApiBaseUrl", () => {
  it("maps the local k8s frontend port to the local k8s API port when no explicit URL is set", () => {
    expect(resolveApiBaseUrl(undefined, "http://127.0.0.1:15175/")).toBe("http://127.0.0.1:18080");
  });
});

describe("buildRequestInit", () => {
  it("includes credentials on read requests", async () => {
    const init = await buildRequestInit();

    expect(init.credentials).toBe("include");
  });

  it("adds content type, credentials, and CSRF token on mutating requests", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(JSON.stringify({
      headerName: "X-CSRF-TOKEN",
      token: "csrf-token"
    }))));

    const init = await buildRequestInit({ body: "{}", method: "POST" });
    const headers = init.headers as Headers;

    expect(init.credentials).toBe("include");
    expect(headers.get("Content-Type")).toBe("application/json");
    expect(headers.get("X-CSRF-TOKEN")).toBe("csrf-token");
  });
});
