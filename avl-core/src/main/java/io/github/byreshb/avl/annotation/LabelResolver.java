package io.github.byreshb.avl.annotation;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Resolves the effective {@code layer}, {@code team}, {@code priority} and {@code component} labels
 * for a test method, applying method-over-class precedence.
 */
public interface LabelResolver {

  /**
   * Resolves the effective labels for a test method.
   *
   * @param testClass the class declaring the test method
   * @param testMethod the test method
   * @return the lower-cased label names (a subset of {@code layer}, {@code team}, {@code priority},
   *     {@code component}) mapped to their effective values; a label is absent from the map when
   *     neither the method nor the class declares it
   */
  Map<String, String> resolve(Class<?> testClass, Method testMethod);
}
