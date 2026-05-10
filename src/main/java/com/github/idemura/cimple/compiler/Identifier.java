package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.compiler.Constants.BUILTIN_MODULE;

public record Identifier(String moduleName, String typeName, String entityName)
    implements Comparable<Identifier> {
  public static Identifier ofEntity(String entityName) {
    return new Identifier(null, null, entityName);
  }

  public static Identifier ofType(String typeName) {
    return new Identifier(null, typeName, null);
  }

  public static Identifier ofTypeEntity(String typeName, String entityName) {
    return new Identifier(null, typeName, entityName);
  }

  public boolean isBuiltin() {
    return BUILTIN_MODULE.equals(moduleName);
  }

  public Identifier builtin() {
    return withModule(BUILTIN_MODULE);
  }

  public Identifier withModule(String moduleName) {
    return new Identifier(moduleName, typeName, entityName);
  }

  public Identifier withType(String typeName) {
    return new Identifier(moduleName, typeName, entityName);
  }

  public Identifier withEntity(String entityName) {
    return new Identifier(moduleName, typeName, entityName);
  }

  @Override
  public int compareTo(Identifier other) {
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
