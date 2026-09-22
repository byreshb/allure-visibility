package io.github.byreshb.avl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the free-text team or squad that owns a test.
 *
 * <p>May be placed on a test class, a test method, or both; a method-level {@code @Team} overrides
 * a class-level one, and a class-level {@code @Team} is the default for any method that declares
 * none. The value is validated to be non-blank when it is read, not when the annotation is
 * declared.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Team {

  /** The name of the owning team or squad. */
  String value();
}
