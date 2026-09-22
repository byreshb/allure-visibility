package io.github.byreshb.avl.config;

import java.util.Map;

/**
 * Attaches resolved labels to the current Allure test result and applies {@link AvlConfig}'s
 * enforcement policy to whichever required labels are missing.
 */
public interface LabelEnforcer {

  /**
   * Attaches the resolved labels to the current Allure test result, then enforces policy.
   *
   * @param resolvedLabels the lower-cased label names mapped to their effective values
   * @param testDisplayName the name of the test being enforced, for messages and logging
   * @throws AvlPolicyViolationException if a required label is missing and enforcement is {@link
   *     AvlConfig.Enforcement#FAIL}
   */
  void enforce(Map<String, String> resolvedLabels, String testDisplayName);
}
