package io.github.byreshb.avl.audit;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * A single test method found by static analysis, with its resolved labels and whichever required
 * labels it is missing.
 *
 * @param className the fully qualified name of the class declaring the test method
 * @param methodName the test method's name
 * @param sourceFile the source file the method was found in, relative to the audited source root
 * @param resolvedLabels the lower-cased label names mapped to their effective values, applying
 *     method-over-class precedence
 * @param missingRequiredLabels the required labels absent from {@code resolvedLabels}
 */
public record TestFinding(
    String className,
    String methodName,
    Path sourceFile,
    Map<String, String> resolvedLabels,
    Set<String> missingRequiredLabels) {

  /**
   * The test's display name.
   *
   * @return {@code className#methodName}
   */
  public String displayName() {
    return className + "#" + methodName;
  }

  /**
   * Whether the test carries every required label.
   *
   * @return {@code true} if {@link #missingRequiredLabels()} is empty
   */
  public boolean isCompliant() {
    return missingRequiredLabels.isEmpty();
  }
}
