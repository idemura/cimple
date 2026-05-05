package com.github.idemura.cimple.compiler.ast;

public final class AstCast extends AstExpression {
  private AstExpression expression;
  private AstTypeRef typeRef;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstTypeRef getType() {
    return typeRef;
  }

  public AstExpression getExpression() {
    return expression;
  }

  public void setExpression(AstExpression expression) {
    this.expression = expression;
  }

  public AstTypeRef getTypeRef() {
    return typeRef;
  }

  public void setTypeRef(AstTypeRef typeRef) {
    this.typeRef = typeRef;
  }
}
