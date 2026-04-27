package com.github.idemura.cimple.compiler.ast;

public final class AstReturn extends AstStatement {
  private AstExpression expression;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  public AstExpression getExpression() {
    return expression;
  }

  public void setExpression(AstExpression expression) {
    this.expression = expression;
  }
}
