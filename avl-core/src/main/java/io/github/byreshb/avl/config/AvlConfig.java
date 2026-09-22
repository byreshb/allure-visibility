package io.github.byreshb.avl.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/**
 * Runtime configuration for allure-visibility, loaded once per JVM.
 *
 * <p>Each key is resolved in order, first found wins: a system property, then the {@code
 * allure.properties} file on the classpath (read independently of Allure's own loading of that
 * file, since Allure's loader is not public API), then a built-in default.
 *
 * <ul>
 *   <li>{@code avl.required.labels}: a comma-separated subset of {@code team}, {@code layer},
 *       {@code priority}, {@code component}. Defaults to {@code team,layer}.
 *   <li>{@code avl.enforcement}: {@code warn} or {@code fail}. Defaults to {@code warn}.
 * </ul>
 */
public final class AvlConfig {

  private static final String REQUIRED_LABELS_KEY = "avl.required.labels";
  private static final String ENFORCEMENT_KEY = "avl.enforcement";
  private static final String ALLURE_PROPERTIES_RESOURCE = "allure.properties";

  private static final Set<String> KNOWN_LABELS = Set.of("team", "layer", "priority", "component");
  private static final Set<String> DEFAULT_REQUIRED_LABELS = Set.of("team", "layer");
  private static final Enforcement DEFAULT_ENFORCEMENT = Enforcement.WARN;

  private static volatile AvlConfig instance;

  private final Set<String> requiredLabels;
  private final Enforcement enforcement;

  private AvlConfig(Set<String> requiredLabels, Enforcement enforcement) {
    this.requiredLabels = requiredLabels;
    this.enforcement = enforcement;
  }

  /**
   * Returns the JVM-wide configuration instance, loading it on first use.
   *
   * @return the shared configuration instance
   */
  public static AvlConfig getInstance() {
    AvlConfig result = instance;
    if (result == null) {
      synchronized (AvlConfig.class) {
        result = instance;
        if (result == null) {
          result = load();
          instance = result;
        }
      }
    }
    return result;
  }

  /**
   * The required labels, as lower-cased names.
   *
   * @return an immutable, non-empty subset of {@code team}, {@code layer}, {@code priority}, {@code
   *     component}
   */
  public Set<String> requiredLabels() {
    return requiredLabels;
  }

  /**
   * The enforcement policy for a test missing a required label.
   *
   * @return the enforcement policy
   */
  public Enforcement enforcement() {
    return enforcement;
  }

  /**
   * Builds a configuration from the given properties, independently of the JVM-wide singleton
   * returned by {@link #getInstance()}. Each key is still resolved system-property-first,
   * classpath-second, default-third.
   *
   * @param systemProperties consulted first for each {@code avl.*} key
   * @param classpathProperties consulted second for each {@code avl.*} key
   * @return a new, independent configuration instance
   */
  public static AvlConfig load(Properties systemProperties, Properties classpathProperties) {
    Set<String> requiredLabels =
        parseRequiredLabels(
            resolveValue(REQUIRED_LABELS_KEY, systemProperties, classpathProperties));
    Enforcement enforcement =
        parseEnforcement(resolveValue(ENFORCEMENT_KEY, systemProperties, classpathProperties));
    return new AvlConfig(requiredLabels, enforcement);
  }

  private static AvlConfig load() {
    return load(System.getProperties(), loadClasspathProperties(AvlConfig.class.getClassLoader()));
  }

  static Properties loadClasspathProperties(ClassLoader classLoader) {
    Properties properties = new Properties();
    try (InputStream in = classLoader.getResourceAsStream(ALLURE_PROPERTIES_RESOURCE)) {
      if (in != null) {
        properties.load(in);
      }
    } catch (IOException e) {
      throw new IllegalStateException(
          "Failed to read " + ALLURE_PROPERTIES_RESOURCE + " from the classpath", e);
    }
    return properties;
  }

  private static String resolveValue(
      String key, Properties systemProperties, Properties classpathProperties) {
    String systemValue = systemProperties.getProperty(key);
    if (systemValue != null && !systemValue.isBlank()) {
      return systemValue;
    }
    String classpathValue = classpathProperties.getProperty(key);
    if (classpathValue != null && !classpathValue.isBlank()) {
      return classpathValue;
    }
    return null;
  }

  private static Set<String> parseRequiredLabels(String rawValue) {
    if (rawValue == null) {
      return DEFAULT_REQUIRED_LABELS;
    }
    Set<String> labels = new LinkedHashSet<>();
    for (String token : rawValue.split(",")) {
      String label = token.trim().toLowerCase(Locale.ROOT);
      if (label.isEmpty()) {
        continue;
      }
      if (!KNOWN_LABELS.contains(label)) {
        throw new IllegalArgumentException(
            "Unknown label '"
                + label
                + "' in "
                + REQUIRED_LABELS_KEY
                + "; expected a comma-separated subset of "
                + KNOWN_LABELS);
      }
      labels.add(label);
    }
    if (labels.isEmpty()) {
      throw new IllegalArgumentException(REQUIRED_LABELS_KEY + " must not be empty");
    }
    return Collections.unmodifiableSet(labels);
  }

  private static Enforcement parseEnforcement(String rawValue) {
    if (rawValue == null) {
      return DEFAULT_ENFORCEMENT;
    }
    try {
      return Enforcement.valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          ENFORCEMENT_KEY + " must be 'warn' or 'fail', got '" + rawValue + "'", e);
    }
  }

  /** The enforcement policy applied to a test missing a required label. */
  public enum Enforcement {

    /** Attach an {@code avl.compliance=warn:<missing-labels>} label and log; the test passes. */
    WARN,

    /** Throw an {@code AvlPolicyViolationException}, failing the test. */
    FAIL
  }
}
