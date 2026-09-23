export interface AllureLabel {
  name: string;
  value: string;
}

export interface AllureTestResult {
  name: string;
  fullName: string | undefined;
  labels: AllureLabel[];
}

export const REQUIRED_LABEL_NAMES = ["team", "layer", "priority", "component"] as const;

export type LabelName = (typeof REQUIRED_LABEL_NAMES)[number];

export interface LabelCoverage {
  count: number;
  percentage: number;
}

export interface MissingLabelEntry {
  testName: string;
  missingLabels: LabelName[];
}

export interface ComplianceSummary {
  totalTests: number;
  requiredLabels: LabelName[];
  coverage: Record<LabelName, LabelCoverage>;
  byTeam: Record<string, number>;
  byLayer: Record<string, number>;
  missing: MissingLabelEntry[];
}

export type OutputFormat = "console" | "json" | "md";
