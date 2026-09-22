package io.github.byreshb.avl.annotation;

/**
 * The business urgency of fixing a test's subject when it fails.
 *
 * <p>Used by {@link Priority} to attach a {@code priority} Allure label to a test.
 *
 * <p>This is distinct from Allure's own {@code @Severity}: severity describes technical or
 * functional impact, how badly the system breaks, while priority describes how soon that break must
 * be fixed relative to everything else in the backlog. The two often agree but do not have to; a
 * cosmetic bug in a flagship checkout flow can be high priority and low severity at the same time.
 */
public enum TestPriority {

  /** Must be fixed immediately; blocks release or is actively harming users. */
  P0,

  /** Should be fixed in the current cycle. */
  P1,

  /** Should be fixed soon, but does not block the current cycle. */
  P2,

  /** Fixed when convenient; no immediate urgency. */
  P3
}
