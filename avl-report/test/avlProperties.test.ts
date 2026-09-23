import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";
import { DEFAULT_REQUIRED_LABELS, readRequiredLabels } from "../src/avlProperties.js";

const fixturesDir = join(dirname(fileURLToPath(import.meta.url)), "..", "fixtures");

describe("readRequiredLabels", () => {
  it("reads avl.required.labels from a real allure.properties file", async () => {
    const labels = await readRequiredLabels(join(fixturesDir, "allure.properties"));
    expect(labels).toEqual(["team", "layer"]);
  });

  it("trims whitespace, lower-cases and drops unknown labels", async () => {
    const labels = await readRequiredLabels(join(fixturesDir, "edge-case.properties"));
    expect(labels).toEqual(["priority", "component"]);
  });

  it("falls back to the default when no path is given", async () => {
    const labels = await readRequiredLabels(undefined);
    expect(labels).toEqual([...DEFAULT_REQUIRED_LABELS]);
  });

  it("falls back to the default when the file does not exist", async () => {
    const labels = await readRequiredLabels(join(fixturesDir, "does-not-exist.properties"));
    expect(labels).toEqual([...DEFAULT_REQUIRED_LABELS]);
  });
});
