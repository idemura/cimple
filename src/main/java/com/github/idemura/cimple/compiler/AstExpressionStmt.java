package com.github.idemura.cimple.compiler;

// Assignment expression
public class AstExpressionStmt extends AstStatement {
  private AstExpression expression;

  AstExpressionStmt() {}

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  void setExpression(AstExpression expression) {
    this.expression = expression;
  }

  AstExpression getExpression() {
    return expression;
  }
}
