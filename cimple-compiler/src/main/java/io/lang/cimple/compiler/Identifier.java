package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.Constants.BUILTIN_MODULE;

// Type and entity only present for methods.
public record Identifier(String module, String type, String entity)
    implements Comparable<Identifier> {
  public static Identifier ofEntity(String entity) {
    return new Identifier(null, null, entity);
  }

  public static Identifier ofType(String type) {
    return new Identifier(null, type, null);
  }

  public static Identifier ofMethod(String type, String entity) {
    return new Identifier(null, type, entity);
  }

  public boolean isBuiltin() {
    return BUILTIN_MODULE.equals(module);
  }

  public Identifier builtin() {
    return withModule(BUILTIN_MODULE);
  }

  public Identifier withModule(String module) {
    return new Identifier(module, type, entity);
  }

  public Identifier withType(String type) {
    return new Identifier(module, type, entity);
  }

  public Identifier withEntity(String entity) {
    return new Identifier(module, type, entity);
  }

  @Override
  public int compareTo(Identifier other) {
    var cmp = compareNullable(module, other.module);
    if (cmp != 0) {
      return cmp;
    }
    cmp = compareNullable(type, other.type);
    if (cmp != 0) {
      return cmp;
    }
    return compareNullable(entity, other.entity);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    if (module != null) {
      sb.append(module);
      sb.append("~");
    }
    if (entity != null) {
      if (type != null) {
        sb.append(type);
        sb.append(".");
      }
      sb.append(entity);
    } else {
      sb.append(type);
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
