package io.github.byreshb.avl.annotation;

/**
 * The architectural layer a test exercises.
 *
 * <p>Used by {@link Layer} to attach a {@code layer} Allure label to a test.
 */
public enum TestLayer {

  /** A test that exercises a single unit in isolation. */
  UNIT,

  /** A test that exercises the interaction between multiple components. */
  INTEGRATION,

  /** A test that exercises a service through its API. */
  API,

  /** A test that exercises a user interface. */
  UI,

  /** A test that exercises a full, user-facing flow end to end. */
  E2E
}
