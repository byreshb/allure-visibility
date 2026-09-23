import { readFile } from "node:fs/promises";
import { REQUIRED_LABEL_NAMES, type LabelName } from "./types.js";

/** Matches `AvlConfig`'s own default, so the Java-side policy and this report agree. */
export const DEFAULT_REQUIRED_LABELS: readonly LabelName[] = ["team", "layer"];

/**
 * Reads `avl.required.labels` from an `allure.properties` file, the same key and shape
 * `AvlConfig` reads on the Java side. Falls back to {@link DEFAULT_REQUIRED_LABELS} when no path
 * is given, the file doesn't exist, or the key is absent or empty.
 */
export async function readRequiredLabels(propertiesPath?: string): Promise<LabelName[]> {
  if (!propertiesPath) {
    return [...DEFAULT_REQUIRED_LABELS];
  }

  let raw: string;
  try {
    raw = await readFile(propertiesPath, "utf8");
  } catch {
    return [...DEFAULT_REQUIRED_LABELS];
  }

  const properties = parseProperties(raw);
  const rawValue = properties.get("avl.required.labels");
  if (!rawValue) {
    return [...DEFAULT_REQUIRED_LABELS];
  }

  const labels = rawValue
    .split(",")
    .map((token) => token.trim().toLowerCase())
    .filter((token): token is LabelName =>
      (REQUIRED_LABEL_NAMES as readonly string[]).includes(token),
    );

  return labels.length > 0 ? labels : [...DEFAULT_REQUIRED_LABELS];
}

function parseProperties(raw: string): Map<string, string> {
  const properties = new Map<string, string>();
  for (const line of raw.split(/\r?\n/)) {
    const trimmed = line.trim();
    if (trimmed.length === 0 || trimmed.startsWith("#") || trimmed.startsWith("!")) {
      continue;
    }
    const separatorIndex = trimmed.search(/[=:]/);
    if (separatorIndex === -1) {
      continue;
    }
    const key = trimmed.slice(0, separatorIndex).trim();
    const value = trimmed.slice(separatorIndex + 1).trim();
    properties.set(key, value);
  }
  return properties;
}
