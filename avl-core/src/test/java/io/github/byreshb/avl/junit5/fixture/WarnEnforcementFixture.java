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
 * Missing the required {@code team} label under {@code avl.enforcement=warn}: the test still
 * passes, and its Allure result is expected to carry an {@code avl.compliance=warn:team} label. Not
 * picked up directly by Surefire; driven through the JUnit Platform {@code Launcher} by the
 * integration test in the parent package.
 */
@Layer(TestLayer.API)
public class WarnEnforcementFixture {

  @RegisterExtension
  static final AllureVisibilityExtension EXTENSION =
      new AllureVisibilityExtension(
          new DefaultLabelResolver(), new DefaultLabelEnforcer(warnEnforcementConfig()));

  @Test
  void missingTeamStillPasses() {}

  private static AvlConfig warnEnforcementConfig() {
    Properties classpathProperties = new Properties();
    classpathProperties.setProperty("avl.required.labels", "team,layer");
    classpathProperties.setProperty("avl.enforcement", "warn");
    return AvlConfig.load(new Properties(), classpathProperties);
  }
}
