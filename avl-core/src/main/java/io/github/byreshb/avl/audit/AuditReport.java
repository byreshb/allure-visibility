package io.github.byreshb.avl.audit;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The result of auditing a source tree for required label coverage, without running anything.
 *
 * @param totalTests the number of test methods found
 * @param requiredLabels the required labels the audit was run against
 * @param findings every test method found, with its resolved and missing labels
 */
public record AuditReport(int totalTests, Set<String> requiredLabels, List<TestFinding> findings) {

  /**
   * The number of tests carrying every required label.
   *
   * @return the compliant test count
   */
  public long compliantCount() {
    return findings.stream().filter(TestFinding::isCompliant).count();
  }

  /**
   * Every required label mapped to the tests missing it.
   *
   * @return an entry per required label, in the same order as {@link #requiredLabels()}, each
   *     mapped to the (possibly empty) list of tests missing that label
   */
  public Map<String, List<TestFinding>> missingByLabel() {
    Map<String, List<TestFinding>> result = new TreeMap<>();
    for (String label : requiredLabels) {
      result.put(
          label, findings.stream().filter(f -> f.missingRequiredLabels().contains(label)).toList());
    }
    return result;
  }

  /**
   * Every source file mapped to the tests found in it.
   *
   * @return a per-file breakdown of the findings, ordered by file path
   */
  public Map<Path, List<TestFinding>> byFile() {
    return findings.stream()
        .collect(Collectors.groupingBy(TestFinding::sourceFile, TreeMap::new, Collectors.toList()));
  }
}
