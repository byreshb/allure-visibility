package io.github.byreshb.avl.testng.fixture;

import io.github.byreshb.avl.annotation.Layer;
import io.github.byreshb.avl.annotation.TestLayer;
import org.testng.annotations.Test;

/**
 * Missing the required {@code team} label. The integration test in the parent package attaches a
 * separately configured {@code AllureVisibilityListener} (built with {@code avl.enforcement=fail})
 * to prove the test fails with {@code AvlPolicyViolationException}, naming the missing label. Not
 * picked up directly by Surefire; driven through {@code org.testng.TestNG} by that integration
 * test.
 */
@Layer(TestLayer.API)
public class FailEnforcementFixture {

  @Test
  public void missingTeamFailsTheTest() {}
}
