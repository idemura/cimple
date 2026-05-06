package com.github.idemura.cimple.compiler;

public record QualifiedName(String moduleName, String name) implements Comparable<QualifiedName> {
  public QualifiedName(String name) {
    this(null, name);
  }

  public QualifiedName withModuleName(String moduleName) {
    return new QualifiedName(moduleName, name);
  }

  @Override
  public int compareTo(QualifiedName other) {
    var cmp = compareNullable(moduleName, other.moduleName);
    if (cmp != 0) {
      return cmp;
    }
    return name.compareTo(other.name);
  }

  @Override
  public String toString() {
    if (moduleName == null || moduleName.isEmpty()) {
      return name;
    }
    return moduleName + "::" + name;
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
