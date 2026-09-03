package io.lang.cimple.compiler;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

public final class Identifier implements Comparable<Identifier> {
  public static final String BUILTIN_MODULE = "_builtin";

  private Location location;
  private String module;
  private String entity;

  public Identifier(Location location, String module, String entity) {
    this.location = location;
    this.module = module;
    this.entity = entity;
  }

  public Identifier(String entity) {
    this(null, null, entity);
  }

  public boolean isBuiltin() {
    return BUILTIN_MODULE.equals(module);
  }

  public Identifier builtin() {
    return copy().module(BUILTIN_MODULE);
  }

  public Identifier copy() {
    return new Identifier(location, module, entity);
  }

  public void assign(Identifier other) {
    this.location = other.location;
    this.module = other.module;
    this.entity = other.entity;
  }

  public Location location() {
    return location;
  }

  public Identifier location(Location location) {
    this.location = location;
    return this;
  }

  public String module() {
    return module;
  }

  public Identifier module(String module) {
    this.module = checkNotNull(module);
    return this;
  }

  public String entity() {
    return entity;
  }

  public Identifier entity(String entity) {
    this.entity = checkNotNull(entity);
    return this;
  }

  @Override
  public int compareTo(Identifier other) {
    var cmp = compareNullable(module, other.module);
    if (cmp != 0) {
      return cmp;
    }
    return compareNullable(entity, other.entity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(module, entity);
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof Identifier other
            && Objects.equals(module, other.module)
            && Objects.equals(entity, other.entity));
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    if (module != null) {
      sb.append(module);
      sb.append("~");
    }
    sb.append(entity);
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
