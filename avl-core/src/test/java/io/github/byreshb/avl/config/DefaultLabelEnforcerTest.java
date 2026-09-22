package io.github.byreshb.avl.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class DefaultLabelEnforcerTest {

  @Test
  void doesNothingWhenAllRequiredLabelsArePresent() {
    DefaultLabelEnforcer enforcer = new DefaultLabelEnforcer(configWith("team,layer", "fail"));

    assertThatCode(
            () -> enforcer.enforce(Map.of("team", "payments", "layer", "API"), "SomeTest#method"))
        .doesNotThrowAnyException();
  }

  @Test
  void doesNotThrowUnderWarnEnforcementWhenARequiredLabelIsMissing() {
    DefaultLabelEnforcer enforcer = new DefaultLabelEnforcer(configWith("team,layer", "warn"));

    assertThatCode(() -> enforcer.enforce(Map.of("layer", "API"), "SomeTest#method"))
        .doesNotThrowAnyException();
  }

  @Test
  void throwsUnderFailEnforcementWhenARequiredLabelIsMissing() {
    DefaultLabelEnforcer enforcer = new DefaultLabelEnforcer(configWith("team,layer", "fail"));

    assertThatThrownBy(() -> enforcer.enforce(Map.of("layer", "API"), "SomeTest#method"))
        .isInstanceOf(AvlPolicyViolationException.class)
        .hasMessageContaining("SomeTest#method")
        .hasMessageContaining("team");
  }

  @Test
  void namesEveryMissingRequiredLabel() {
    DefaultLabelEnforcer enforcer =
        new DefaultLabelEnforcer(configWith("team,layer,priority", "fail"));

    assertThatThrownBy(() -> enforcer.enforce(Map.of(), "SomeTest#method"))
        .hasMessageContaining("team")
        .hasMessageContaining("layer")
        .hasMessageContaining("priority");
  }

  private AvlConfig configWith(String requiredLabels, String enforcement) {
    Properties properties = new Properties();
    properties.setProperty("avl.required.labels", requiredLabels);
    properties.setProperty("avl.enforcement", enforcement);
    return AvlConfig.load(new Properties(), properties);
  }
}
