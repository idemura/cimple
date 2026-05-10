package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.compiler.Constants.BUILTIN_MODULE;

public record QualifiedName(String moduleName, String baseName)
    implements Comparable<QualifiedName> {
  public QualifiedName(String baseName) {
    this(null, baseName);
  }

  public static QualifiedName ofBuiltin(String baseName) {
    return new QualifiedName(BUILTIN_MODULE, baseName);
  }

  public boolean isBuiltin() {
    return BUILTIN_MODULE.equals(moduleName);
  }

  public QualifiedName withModuleName(String moduleName) {
    return new QualifiedName(moduleName, baseName);
  }

  @Override
  public int compareTo(QualifiedName other) {
    var cmp = compareNullable(moduleName, other.moduleName);
    if (cmp != 0) {
      return cmp;
    }
    return baseName.compareTo(other.baseName);
  }

  @Override
  public String toString() {
    if (moduleName == null || isBuiltin()) {
      return baseName;
    }
    return moduleName + "~" + baseName;
  }

  private static int compareNullable(String left, String right) {
    if (left == null) {
      return right == null ? 0 : -1;
    }
    if (right == null) {
      return 1;
    }
    return left.compareTo(right);
  }
}
