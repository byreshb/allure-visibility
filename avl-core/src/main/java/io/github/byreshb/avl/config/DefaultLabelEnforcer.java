package io.github.byreshb.avl.config;

import io.qameta.allure.Allure;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/** Default {@link LabelEnforcer}, backed by {@link Allure#label(String, String)}. */
public final class DefaultLabelEnforcer implements LabelEnforcer {

  private static final Logger LOGGER = Logger.getLogger(DefaultLabelEnforcer.class.getName());
  private static final String COMPLIANCE_LABEL = "avl.compliance";

  private final AvlConfig config;

  /**
   * Creates an enforcer bound to the given configuration.
   *
   * @param config the configuration naming the required labels and the enforcement policy
   */
  public DefaultLabelEnforcer(AvlConfig config) {
    this.config = config;
  }

  @Override
  public void enforce(Map<String, String> resolvedLabels, String testDisplayName) {
    resolvedLabels.forEach(Allure::label);

    Set<String> missing = missingRequiredLabels(resolvedLabels);
    if (missing.isEmpty()) {
      return;
    }

    if (config.enforcement() == AvlConfig.Enforcement.FAIL) {
      throw new AvlPolicyViolationException(testDisplayName, missing);
    }

    String missingList = String.join(",", missing);
    Allure.label(COMPLIANCE_LABEL, "warn:" + missingList);
    LOGGER.warning(
        () -> "\"" + testDisplayName + "\" is missing required label(s): " + missingList);
  }

  private Set<String> missingRequiredLabels(Map<String, String> resolvedLabels) {
    Set<String> missing = new LinkedHashSet<>();
    for (String required : config.requiredLabels()) {
      if (!resolvedLabels.containsKey(required)) {
        missing.add(required);
      }
    }
    return missing;
  }
}
