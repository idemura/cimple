package io.lang.cimple.compiler;

import static com.google.common.base.Preconditions.checkArgument;
import static io.lang.cimple.compiler.Constants.BUILTIN_MODULE;

import java.util.Objects;

public final class Identifier implements Comparable<Identifier> {
  private String module;
  private String type;
  private String entity;

  public Identifier(String module, String type, String entity) {
    checkArgument((type != null) ^ (entity != null));
    this.module = module;
    this.type = type;
    this.entity = entity;
  }

  public static Identifier of(String entity) {
    return new Identifier(null, null, entity);
  }

  public static Identifier ofType(String type) {
    return new Identifier(null, type, null);
  }

  public boolean isBuiltin() {
    return BUILTIN_MODULE.equals(module);
  }

  public Identifier builtin() {
    return copy().module(BUILTIN_MODULE);
  }

  public Identifier copy() {
    return new Identifier(module, type, entity);
  }

  public Identifier copyValue(Identifier other) {
    this.module = other.module;
    this.type = other.type;
    this.entity = other.entity;
    return this;
  }

  public String module() {
    return module;
  }

  public Identifier module(String module) {
    this.module = module;
    return this;
  }

  public String type() {
    return type;
  }

  public Identifier type(String type) {
    this.type = type;
    return this;
  }

  public String entity() {
    return entity;
  }

  public Identifier entity(String entity) {
    this.entity = entity;
    return this;
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
  public int hashCode() {
    return Objects.hash(module, type, entity);
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof Identifier other
            && Objects.equals(module, other.module)
            && Objects.equals(type, other.type)
            && Objects.equals(entity, other.entity));
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
