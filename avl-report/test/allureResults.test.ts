import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";
import { readAllureResults } from "../src/allureResults.js";

const fixturesDir = join(
  dirname(fileURLToPath(import.meta.url)),
  "..",
  "fixtures",
  "allure-results",
);

describe("readAllureResults", () => {
  it("reads every *-result.json file and ignores container files", async () => {
    const results = await readAllureResults(fixturesDir);

    expect(results).toHaveLength(3);
    expect(results.map((r) => r.fullName)).toEqual([
      "sample.CheckoutApiTest.appliesPromoCodeToTotal",
      "sample.CheckoutApiTest.missingTeam",
      "sample.BillingExportTest.missingLayer",
    ]);
  });

  it("extracts labels for each result", async () => {
    const results = await readAllureResults(fixturesDir);
    const compliant = results.find((r) => r.name === "appliesPromoCodeToTotal");

    expect(compliant?.labels).toEqual([
      { name: "layer", value: "API" },
      { name: "team", value: "payments" },
    ]);
  });
});
