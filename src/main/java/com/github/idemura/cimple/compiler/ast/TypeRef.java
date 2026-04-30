package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public final class TypeRef {
  private final QualifiedName name;
  private AstType type;

  public static TypeRef of(String name) {
    return new TypeRef(new QualifiedName(name));
  }

  public static TypeRef of(AstType type) {
    var typeRef = new TypeRef(type.getName());
    typeRef.setType(type);
    return typeRef;
  }

  public TypeRef(QualifiedName name) {
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

  public QualifiedName name() {
    return name;
  }

  public AstType getType() {
    return type;
  }

  public void setType(AstType type) {
    this.type = type;
  }
}
