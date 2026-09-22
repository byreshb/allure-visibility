package io.github.byreshb.avl.junit5.fixture;

import io.github.byreshb.avl.annotation.Layer;
import io.github.byreshb.avl.annotation.Team;
import io.github.byreshb.avl.annotation.TestLayer;
import io.github.byreshb.avl.junit5.AllureVisibilityExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Carries every required label. Demonstrates {@link AllureVisibilityExtension}'s primary,
 * documented usage: explicit {@code @ExtendWith}. Not picked up directly by Surefire; driven
 * through the JUnit Platform {@code Launcher} by the integration test in the parent package.
 */
@ExtendWith(AllureVisibilityExtension.class)
@Layer(TestLayer.API)
@Team("payments")
public class CompliantFixture {

  @Test
  void appliesPromoCodeToTotal() {}
}
