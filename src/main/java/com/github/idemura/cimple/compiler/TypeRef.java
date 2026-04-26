package com.github.idemura.cimple.compiler;

public class TypeRef {
  final String name;

  TypeRef(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object || (object instanceof TypeRef other && name.equals(other.name));
  }

  @Override
  public String toString() {
    return "TYPE_REF({%s})".formatted(name);
  }

  public String getName() {
    return name;
  }
}
