package io.github.byreshb.avl.testng;

import io.github.byreshb.avl.annotation.DefaultLabelResolver;
import io.github.byreshb.avl.annotation.LabelResolver;
import io.github.byreshb.avl.config.AvlConfig;
import io.github.byreshb.avl.config.DefaultLabelEnforcer;
import io.github.byreshb.avl.config.LabelEnforcer;
import java.lang.reflect.Method;
import java.util.Map;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

/**
 * Attaches {@code layer}/{@code team}/{@code priority}/{@code component} Allure labels to each test
 * and enforces {@link AvlConfig}'s policy for required labels.
 *
 * <p>Implements {@link IInvokedMethodListener} rather than {@code ITestListener}: {@code
 * allure-testng}'s own listener starts the Allure lifecycle test case from {@code
 * ITestListener#onTestStart}, and TestNG gives no ordering guarantee between two {@code
 * ServiceLoader}-discovered {@code ITestListener}s, so a plain {@code onTestStart} here could run
 * before or after Allure's, silently dropping every label. {@code beforeInvocation} is guaranteed
 * to run only after every {@code onTestStart} notification, including Allure's, has completed.
 *
 * <p>Can be registered explicitly, the same way as the JUnit 5 extension, with
 * {@code @Listeners(AllureVisibilityListener.class)} on a test class. It is also registered under
 * {@code META-INF/services/org.testng.ITestNGListener}, and TestNG loads {@code
 * ServiceLoader}-declared listeners automatically, with no opt-in flag — a genuine difference from
 * the JUnit 5 module, where extension autodetection must be turned on explicitly.
 */
public final class AllureVisibilityListener implements IInvokedMethodListener {

  private final LabelResolver labelResolver;
  private final LabelEnforcer labelEnforcer;

  /** Creates a listener using the default resolver and the JVM-wide {@link AvlConfig}. */
  public AllureVisibilityListener() {
    this(new DefaultLabelResolver(), new DefaultLabelEnforcer(AvlConfig.getInstance()));
  }

  /**
   * Creates a listener with an explicit resolver and enforcer.
   *
   * @param labelResolver resolves the effective labels for a test
   * @param labelEnforcer attaches labels and applies enforcement policy
   */
  public AllureVisibilityListener(LabelResolver labelResolver, LabelEnforcer labelEnforcer) {
    this.labelResolver = labelResolver;
    this.labelEnforcer = labelEnforcer;
  }

  @Override
  public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
    if (!invokedMethod.isTestMethod()) {
      return;
    }

    Method testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
    Class<?> testClass = testResult.getTestClass().getRealClass();

    Map<String, String> labels = labelResolver.resolve(testClass, testMethod);
    String testDisplayName = testClass.getName() + "#" + testMethod.getName();

    labelEnforcer.enforce(labels, testDisplayName);
  }
}
