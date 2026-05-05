package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.QualifiedName;
import java.util.Objects;

public final class AstTypeRef {
  private final QualifiedName name;
  private AstType type;

  // For testing
  public static AstTypeRef ofString(String name) {
    return new AstTypeRef(new QualifiedName(name));
  }

  public static AstTypeRef of(AstType type) {
    var typeRef = new AstTypeRef(type.getName());
    typeRef.setType(type);
    return typeRef;
  }

  public AstTypeRef(QualifiedName name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstTypeRef other && Objects.equals(name, other.name));
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
