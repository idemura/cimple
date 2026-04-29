package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public final class TypeRef {
  private String name;
  private AstType type;

  public static TypeRef ofName(String name) {
    return new TypeRef(name, null);
  }

  public static TypeRef of(AstType type) {
    return new TypeRef(type.getName(), type);
  }

  private TypeRef(String name, AstType type) {
    this.name = name;
    this.type = type;
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

  public AstType getType() {
    return type;
  }

  public void setType(AstType type) {
    this.type = type;
  }
}
