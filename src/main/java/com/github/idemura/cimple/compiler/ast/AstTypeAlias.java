package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.TypeRef;

public final class AstTypeAlias extends AstType {
  private TypeRef baseTypeRef;

  public AstTypeAlias() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  public TypeRef getBaseTypeRef() {
    return baseTypeRef;
  }

  public void setBaseTypeRef(TypeRef baseTypeRef) {
    this.baseTypeRef = baseTypeRef;
  }
}
