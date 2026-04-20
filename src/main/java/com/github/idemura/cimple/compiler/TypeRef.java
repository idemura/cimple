package com.github.idemura.cimple.compiler;

public class TypeRef {
  final String name;
  final boolean primitive;

  private TypeRef(String name, boolean primitive) {
    this.name = name;
    this.primitive = primitive;
  }

  static TypeRef ofName(String name) {
    var p = BuiltinTypes.NAME_MAP.get(name);
    return p != null ? p : new TypeRef(name, false);
  }

  static TypeRef std(String name) {
    return new TypeRef(name, true);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return this == o || ((o instanceof TypeRef other) && name.equals(other.name));
  }

  @Override
  public String toString() {
    return "TYPE_REF({%s})".formatted(name);
  }

  boolean isPrimitive() {
    return primitive;
  }

  public String getName() {
    return name;
  }
}
