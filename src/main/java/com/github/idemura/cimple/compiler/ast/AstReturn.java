package com.github.idemura.cimple.compiler.ast;

public final class AstReturn extends AstStatement {
  private AstExpression expression;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public AstExpression expression() {
    return expression;
  }

  public void expression(AstExpression expression) {
    this.expression = expression;
  }
}
