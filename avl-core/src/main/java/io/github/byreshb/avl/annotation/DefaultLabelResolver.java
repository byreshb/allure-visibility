package io.github.byreshb.avl.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default {@link LabelResolver} implementation, using plain reflection over the four label
 * annotations with method-over-class precedence.
 */
public final class DefaultLabelResolver implements LabelResolver {

  @Override
  public Map<String, String> resolve(Class<?> testClass, Method testMethod) {
    Map<String, String> labels = new LinkedHashMap<>();
    putLayer(labels, testClass, testMethod);
    putTeam(labels, testClass, testMethod);
    putPriority(labels, testClass, testMethod);
    putComponent(labels, testClass, testMethod);
    return labels;
  }

  private static void putLayer(Map<String, String> labels, Class<?> testClass, Method testMethod) {
    Layer layer = effective(Layer.class, testClass, testMethod);
    if (layer != null) {
      labels.put("layer", layer.value().name());
    }
  }

  private static void putTeam(Map<String, String> labels, Class<?> testClass, Method testMethod) {
    Team team = effective(Team.class, testClass, testMethod);
    if (team != null) {
      labels.put("team", requireNonBlank(team.value(), Team.class, testClass, testMethod));
    }
  }

  private static void putPriority(
      Map<String, String> labels, Class<?> testClass, Method testMethod) {
    Priority priority = effective(Priority.class, testClass, testMethod);
    if (priority != null) {
      labels.put("priority", priority.value().name());
    }
  }

  private static void putComponent(
      Map<String, String> labels, Class<?> testClass, Method testMethod) {
    Component component = effective(Component.class, testClass, testMethod);
    if (component != null) {
      labels.put(
          "component", requireNonBlank(component.value(), Component.class, testClass, testMethod));
    }
  }

  private static <A extends Annotation> A effective(
      Class<A> annotationType, Class<?> testClass, Method testMethod) {
    A onMethod = testMethod.getAnnotation(annotationType);
    return onMethod != null ? onMethod : testClass.getAnnotation(annotationType);
  }

  private static String requireNonBlank(
      String value,
      Class<? extends Annotation> annotationType,
      Class<?> testClass,
      Method testMethod) {
    if (value.isBlank()) {
      throw new IllegalStateException(
          "@"
              + annotationType.getSimpleName()
              + " on "
              + testClass.getName()
              + "#"
              + testMethod.getName()
              + " must not be blank");
    }
    return value;
  }
}
