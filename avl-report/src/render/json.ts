import type { ComplianceSummary } from "../types.js";

/** Renders a {@link ComplianceSummary} as JSON. */
export function renderJson(summary: ComplianceSummary): string {
  return JSON.stringify(summary, null, 2);
}
