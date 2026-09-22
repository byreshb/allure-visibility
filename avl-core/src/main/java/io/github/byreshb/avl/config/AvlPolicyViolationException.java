package io.github.byreshb.avl.config;

import java.util.Set;

/**
 * Thrown when {@link AvlConfig.Enforcement#FAIL} is active and a test is missing one or more of the
 * configured {@link AvlConfig#requiredLabels()}.
 */
public final class AvlPolicyViolationException extends RuntimeException {

  /**
   * Creates an exception naming the test and the labels it is missing.
   *
   * @param testDisplayName the failing test's name
   * @param missingLabels the required labels the test did not declare
   */
  public AvlPolicyViolationException(String testDisplayName, Set<String> missingLabels) {
    super(
        "\""
            + testDisplayName
            + "\" is missing required label(s): "
            + String.join(", ", missingLabels));
  }
}
