package com.github.idemura.cimple.compiler;

public class AstTypeAlias extends AstAbstractType {
  private TypeRef baseTypeRef;

  AstTypeAlias() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  TypeRef getBaseTypeRef() {
    return baseTypeRef;
  }

  void setBaseTypeRef(TypeRef baseTypeRef) {
    this.baseTypeRef = baseTypeRef;
  }
}
