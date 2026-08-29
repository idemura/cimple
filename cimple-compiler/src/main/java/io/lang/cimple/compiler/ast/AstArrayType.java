package io.lang.cimple.compiler.ast;

import io.lang.cimple.compiler.Identifier;

public final class AstArrayType extends AstType {
  private AstType baseType;

  public AstArrayType(AstType baseType) {
    this.baseType = baseType;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    baseType.accept(visitor);
  }

  @Override
  public int hashCode() {
    return baseType.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstArrayType other && baseType.equals(other.baseType));
  }

  @Override
  public Identifier name() {
    var baseName = baseType.name();
    return baseName.withType(baseName.type() + "[]");
  }

  @Override
  public void name(Identifier name) {
    throw new UnsupportedOperationException();
  }

  public AstType baseType() {
    return baseType;
  }

  public void baseType(AstType baseType) {
    this.baseType = baseType;
  }
}
