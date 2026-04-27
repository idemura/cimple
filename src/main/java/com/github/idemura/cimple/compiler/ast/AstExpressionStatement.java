package com.github.idemura.cimple.compiler.ast;

public final class AstExpressionStatement extends AstStatement {
  private AstExpression expression;

  public AstExpressionStatement() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  public void setExpression(AstExpression expression) {
    this.expression = expression;
  }

  public AstExpression getExpression() {
    return expression;
  }
}
