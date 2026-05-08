package com.github.idemura.cimple.compiler.ast;

public final class AstCast extends AstExpression {
  private AstExpression expression;
  private AstTypeRef typeRef;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstTypeRef type() {
    return typeRef;
  }

  public void type(AstTypeRef typeRef) {
    this.typeRef = typeRef;
  }

  public AstExpression expression() {
    return expression;
  }

  public void expression(AstExpression expression) {
    this.expression = expression;
  }
}
