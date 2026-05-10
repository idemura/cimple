package com.github.idemura.cimple.compiler.ast;

public final class AstNew extends AstExpression {
  private AstTypeRef typeRef;
  private AstExpression size;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstTypeRef typeRef() {
    return typeRef;
  }

  public void typeRef(AstTypeRef typeRef) {
    this.typeRef = typeRef;
  }

  public AstExpression size() {
    return size;
  }

  public void size(AstExpression size) {
    this.size = size;
  }
}
