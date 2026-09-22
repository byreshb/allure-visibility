package io.github.byreshb.avl.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultLabelResolverTest {

  private final DefaultLabelResolver resolver = new DefaultLabelResolver();

  @Test
  void resolvesLabelsDeclaredOnTheMethod() throws NoSuchMethodException {
    Map<String, String> labels = resolve(MethodLevel.class, "annotated");

    assertThat(labels)
        .containsEntry("layer", "API")
        .containsEntry("team", "payments")
        .containsEntry("priority", "P1")
        .containsEntry("component", "checkout");
  }

  @Test
  void fallsBackToClassLevelWhenMethodHasNoAnnotation() throws NoSuchMethodException {
    Map<String, String> labels = resolve(ClassLevel.class, "unannotated");

    assertThat(labels).containsEntry("layer", "UNIT").containsEntry("team", "platform");
  }

  @Test
  void methodLevelOverridesClassLevel() throws NoSuchMethodException {
    Map<String, String> labels = resolve(Overriding.class, "annotated");

    assertThat(labels).containsEntry("layer", "E2E").containsEntry("team", "checkout");
  }

  @Test
  void omitsLabelsThatAreDeclaredNowhere() throws NoSuchMethodException {
    Map<String, String> labels = resolve(NoAnnotations.class, "plain");

    assertThat(labels).isEmpty();
  }

  @Test
  void rejectsBlankTeamAtReadTime() throws NoSuchMethodException {
    Method method = BlankValues.class.getDeclaredMethod("blankTeam");

    assertThatThrownBy(() -> resolver.resolve(BlankValues.class, method))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("@Team")
        .hasMessageContaining("blankTeam");
  }

  @Test
  void rejectsBlankComponentAtReadTime() throws NoSuchMethodException {
    Method method = BlankValues.class.getDeclaredMethod("blankComponent");

    assertThatThrownBy(() -> resolver.resolve(BlankValues.class, method))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("@Component")
        .hasMessageContaining("blankComponent");
  }

  private Map<String, String> resolve(Class<?> testClass, String methodName)
      throws NoSuchMethodException {
    return resolver.resolve(testClass, testClass.getDeclaredMethod(methodName));
  }

  private static class MethodLevel {
    @Layer(TestLayer.API)
    @Team("payments")
    @Priority(TestPriority.P1)
    @Component("checkout")
    void annotated() {}
  }

  @Layer(TestLayer.UNIT)
  @Team("platform")
  private static class ClassLevel {
    void unannotated() {}
  }

  @Layer(TestLayer.UNIT)
  @Team("platform")
  private static class Overriding {
    @Layer(TestLayer.E2E)
    @Team("checkout")
    void annotated() {}
  }

  private static class NoAnnotations {
    void plain() {}
  }

  private static class BlankValues {
    @Team("   ")
    void blankTeam() {}

    @Component("")
    void blankComponent() {}
  }
}
