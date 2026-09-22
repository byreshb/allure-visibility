package io.github.byreshb.avl.audit;

import java.nio.file.Path;
import java.util.Set;

/**
 * Audits a test source tree for required label coverage using static analysis, without compiling or
 * running anything.
 */
public interface SuiteAuditor {

  /**
   * Audits every test class under a source root.
   *
   * @param sourceRoot the root directory to scan for {@code .java} files
   * @param requiredLabels the lower-cased label names a test must carry to be compliant
   * @return the audit result
   */
  AuditReport audit(Path sourceRoot, Set<String> requiredLabels);
}
