package io.github.byreshb.avl.junit5;

import io.github.byreshb.avl.annotation.DefaultLabelResolver;
import io.github.byreshb.avl.annotation.LabelResolver;
import io.github.byreshb.avl.config.AvlConfig;
import io.github.byreshb.avl.config.DefaultLabelEnforcer;
import io.github.byreshb.avl.config.LabelEnforcer;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Attaches {@code layer}/{@code team}/{@code priority}/{@code component} Allure labels to each test
 * and enforces {@link AvlConfig}'s policy for required labels.
 *
 * <p>Primary usage is explicit: {@code @ExtendWith(AllureVisibilityExtension.class)} on a base test
 * class. It is also registered under {@code
 * META-INF/services/org.junit.jupiter.api.extension.Extension} for JUnit 5's own extension
 * autodetection; that autodetection is opt-in ({@code
 * junit.jupiter.extensions.autodetection.enabled=true} in {@code junit-platform.properties}), not
 * on by default, so the accurate claim is "zero-code registration once autodetection is enabled,"
 * not "automatic with no configuration at all."
 */
public final class AllureVisibilityExtension implements BeforeEachCallback {

  private final LabelResolver labelResolver;
  private final LabelEnforcer labelEnforcer;

  /** Creates an extension using the default resolver and the JVM-wide {@link AvlConfig}. */
  public AllureVisibilityExtension() {
    this(new DefaultLabelResolver(), new DefaultLabelEnforcer(AvlConfig.getInstance()));
  }

  /**
   * Creates an extension with an explicit resolver and enforcer.
   *
   * @param labelResolver resolves the effective labels for a test
   * @param labelEnforcer attaches labels and applies enforcement policy
   */
  public AllureVisibilityExtension(LabelResolver labelResolver, LabelEnforcer labelEnforcer) {
    this.labelResolver = labelResolver;
    this.labelEnforcer = labelEnforcer;
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    Method testMethod = context.getRequiredTestMethod();

    Map<String, String> labels = labelResolver.resolve(testClass, testMethod);
    String testDisplayName = testClass.getName() + "#" + testMethod.getName();

    labelEnforcer.enforce(labels, testDisplayName);
  }
}
