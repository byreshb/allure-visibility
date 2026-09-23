import {
  REQUIRED_LABEL_NAMES,
  type AllureTestResult,
  type ComplianceSummary,
  type LabelName,
} from "./types.js";

/** Builds a compliance summary from parsed Allure results and the configured required labels. */
export function buildComplianceSummary(
  results: AllureTestResult[],
  requiredLabels: LabelName[],
): ComplianceSummary {
  const totalTests = results.length;

  const counts = new Map<LabelName, number>(REQUIRED_LABEL_NAMES.map((label) => [label, 0]));
  const byTeam = new Map<string, number>();
  const byLayer = new Map<string, number>();
  const missing: ComplianceSummary["missing"] = [];

  for (const result of results) {
    const labelValues = new Map(result.labels.map((label) => [label.name, label.value]));

    for (const labelName of REQUIRED_LABEL_NAMES) {
      if (labelValues.has(labelName)) {
        counts.set(labelName, (counts.get(labelName) ?? 0) + 1);
      }
    }

    incrementIfPresent(byTeam, labelValues.get("team"));
    incrementIfPresent(byLayer, labelValues.get("layer"));

    const missingLabels = requiredLabels.filter((label) => !labelValues.has(label));
    if (missingLabels.length > 0) {
      missing.push({ testName: result.fullName ?? result.name, missingLabels });
    }
  }

  const coverage = Object.fromEntries(
    REQUIRED_LABEL_NAMES.map((label) => {
      const count = counts.get(label) ?? 0;
      const percentage = totalTests === 0 ? 0 : Math.round((count / totalTests) * 1000) / 10;
      return [label, { count, percentage }];
    }),
  ) as ComplianceSummary["coverage"];

  return {
    totalTests,
    requiredLabels,
    coverage,
    byTeam: Object.fromEntries(byTeam),
    byLayer: Object.fromEntries(byLayer),
    missing,
  };
}

function incrementIfPresent(counts: Map<string, number>, value: string | undefined): void {
  if (value === undefined) {
    return;
  }
  counts.set(value, (counts.get(value) ?? 0) + 1);
}
