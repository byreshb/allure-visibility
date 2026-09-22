package io.github.byreshb.avl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the free-text product area under test, for example {@code "checkout"} or {@code
 * "billing-export"}.
 *
 * <p>May be placed on a test class, a test method, or both; a method-level {@code @Component}
 * overrides a class-level one, and a class-level {@code @Component} is the default for any method
 * that declares none. The value is validated to be non-blank when it is read, not when the
 * annotation is declared.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Component {

  /** The product area under test. */
  String value();
}
