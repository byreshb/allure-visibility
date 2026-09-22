package io.github.byreshb.avl.testng.fixture;

import io.github.byreshb.avl.annotation.Layer;
import io.github.byreshb.avl.annotation.Team;
import io.github.byreshb.avl.annotation.TestLayer;
import io.github.byreshb.avl.testng.AllureVisibilityListener;
import org.testng.annotations.Test;

/**
 * Carries every required label. Registers no listener at all: {@link AllureVisibilityListener} is
 * expected to apply automatically through TestNG's {@code ServiceLoader}-based listener discovery,
 * proving the genuine difference from the JUnit 5 module documented in {@code docs/testng.md}. Not
 * picked up directly by Surefire; driven through {@code org.testng.TestNG} by the integration test
 * in the parent package.
 */
@Layer(TestLayer.API)
@Team("payments")
public class CompliantFixture {

  @Test
  public void appliesPromoCodeToTotal() {}
}
