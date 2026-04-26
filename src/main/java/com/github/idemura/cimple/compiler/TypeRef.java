package com.github.idemura.cimple.compiler;

import java.util.Objects;

public class TypeRef {
  private String name;
  private Type type;

  public TypeRef(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object || (object instanceof TypeRef other && Objects.equals(name, other.name));
  }

  @Override
  public String toString() {
    return "TYPE_REF(%s)".formatted(name);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
}
