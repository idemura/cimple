package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;

public final class AstPointerType extends AstType {
  private AstType baseType;

  public AstPointerType(AstType baseType) {
    this.baseType = baseType;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return baseType.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstPointerType other && baseType.equals(other.baseType));
  }

  @Override
  public Identifier name() {
    var baseName = baseType.name();
    return baseName.withEntity(baseName.entityName() + "*");
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
