package com.github.idemura.cimple.compiler.ast;

public final class AstTypeAlias extends AstType {
  private TypeRef baseTypeRef;

  public AstTypeAlias() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public TypeRef getBaseTypeRef() {
    return baseTypeRef;
  }

  public void setBaseTypeRef(TypeRef baseTypeRef) {
    this.baseTypeRef = baseTypeRef;
  }
}
