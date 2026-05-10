package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.compiler.Constants.BUILTIN_MODULE;

public record QualifiedName(String moduleName, String typeName, String entityName)
    implements Comparable<QualifiedName> {
  public static QualifiedName ofEntity(String entityName) {
    return new QualifiedName(null, null, entityName);
  }

  public static QualifiedName ofType(String typeName) {
    return new QualifiedName(null, typeName, null);
  }

  public static QualifiedName ofTypeEntity(String typeName, String entityName) {
    return new QualifiedName(null, typeName, entityName);
  }

  public boolean isBuiltin() {
    return BUILTIN_MODULE.equals(moduleName);
  }

  public QualifiedName builtin() {
    return withModule(BUILTIN_MODULE);
  }

  public QualifiedName withModule(String moduleName) {
    return new QualifiedName(moduleName, typeName, entityName);
  }

  public QualifiedName withType(String typeName) {
    return new QualifiedName(moduleName, typeName, entityName);
  }

  public QualifiedName withEntity(String entityName) {
    return new QualifiedName(moduleName, typeName, entityName);
  }

  @Override
  public int compareTo(QualifiedName other) {
    var cmp = compareNullable(moduleName, other.moduleName);
    if (cmp != 0) {
      return cmp;
    }
    cmp = compareNullable(typeName, other.typeName);
    if (cmp != 0) {
      return cmp;
    }
    return compareNullable(entityName, other.entityName);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    if (moduleName != null && !isBuiltin()) {
      sb.append(moduleName);
      sb.append("~");
    }
    if (entityName != null) {
      if (typeName != null) {
        sb.append(typeName);
        sb.append(":");
      }
      sb.append(entityName);
    } else {
      sb.append(typeName);
    }
    return sb.toString();
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
