package io.github.byreshb.avl.junit5.fixture;

import io.github.byreshb.avl.annotation.DefaultLabelResolver;
import io.github.byreshb.avl.annotation.Layer;
import io.github.byreshb.avl.annotation.TestLayer;
import io.github.byreshb.avl.config.AvlConfig;
import io.github.byreshb.avl.config.DefaultLabelEnforcer;
import io.github.byreshb.avl.junit5.AllureVisibilityExtension;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Missing the required {@code team} label under {@code avl.enforcement=fail}: the test fails with
 * {@code AvlPolicyViolationException}, naming the missing label. Not picked up directly by
 * Surefire; driven through the JUnit Platform {@code Launcher} by the integration test in the
 * parent package.
 */
@Layer(TestLayer.API)
public class FailEnforcementFixture {

  @RegisterExtension
  static final AllureVisibilityExtension EXTENSION =
      new AllureVisibilityExtension(
          new DefaultLabelResolver(), new DefaultLabelEnforcer(failEnforcementConfig()));

  @Test
  void missingTeamFailsTheTest() {}

  private static AvlConfig failEnforcementConfig() {
    Properties classpathProperties = new Properties();
    classpathProperties.setProperty("avl.required.labels", "team,layer");
    classpathProperties.setProperty("avl.enforcement", "fail");
    return AvlConfig.load(new Properties(), classpathProperties);
  }
}
