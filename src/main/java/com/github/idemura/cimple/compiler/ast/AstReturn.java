package com.github.idemura.cimple.compiler.ast;

public final class AstReturn extends AstStatement {
  private AstExpressionHolder expression;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public AstExpressionHolder expression() {
    return expression;
  }

  public void expression(AstExpressionHolder expression) {
    this.expression = expression;
  }
}
