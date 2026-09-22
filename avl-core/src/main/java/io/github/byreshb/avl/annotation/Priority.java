package io.github.byreshb.avl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the business {@link TestPriority} of a test's subject.
 *
 * <p>May be placed on a test class, a test method, or both; a method-level {@code @Priority}
 * overrides a class-level one, and a class-level {@code @Priority} is the default for any method
 * that declares none.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Priority {

  /** The priority of this test's subject. */
  TestPriority value();
}
