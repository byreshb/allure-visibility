package io.github.byreshb.avl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the architectural {@link TestLayer} a test exercises.
 *
 * <p>May be placed on a test class, a test method, or both; a method-level {@code @Layer} overrides
 * a class-level one, and a class-level {@code @Layer} is the default for any method that declares
 * none.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Layer {

  /** The layer this test exercises. */
  TestLayer value();
}
