package io.github.byreshb.avl.testng.fixture;

import io.github.byreshb.avl.annotation.Layer;
import io.github.byreshb.avl.annotation.TestLayer;
import org.testng.annotations.Test;

/**
 * Missing the required {@code team} label. The integration test in the parent package attaches a
 * separately configured {@code AllureVisibilityListener} (built with {@code avl.enforcement=warn})
 * to prove the test still passes and is marked non-compliant. Not picked up directly by Surefire;
 * driven through {@code org.testng.TestNG} by that integration test.
 */
@Layer(TestLayer.API)
public class WarnEnforcementFixture {

  @Test
  public void missingTeamStillPasses() {}
}
