package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.QualifiedName;
import java.util.Objects;

public final class AstTypeRef extends AstNode {
  private QualifiedName name;
  private AstType type;

  // For testing
  public static AstTypeRef ofName(String name) {
    return ofName(null, name);
  }

  public static AstTypeRef ofName(String moduleName, String name) {
    var ref = new AstTypeRef();
    ref.setName(new QualifiedName(moduleName, name));
    return ref;
  }

  public static AstTypeRef ofType(AstType type) {
    var ref = new AstTypeRef();
    ref.setName(type.getName());
    ref.setType(type);
    ref.markNameResolved();
    return ref;
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
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

  public QualifiedName getName() {
    return name;
  }

  public void setName(QualifiedName name) {
    this.name = name;
  }

  public AstType getType() {
    return type;
  }

  public void setType(AstType type) {
    this.type = type;
  }
}
