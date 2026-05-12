package com.github.idemura.cimple.compiler.ast;

public final class AstReturn extends AstStatement {
  private AstExpressionHolder expression;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  public AstExpressionHolder expression() {
    return expression;
  }

  public void expression(AstExpressionHolder expression) {
    this.expression = expression;
  }
}
