package com.github.idemura.cimple.compiler;

public class AstExpressionStatement extends AstAbstractStatement {
  private AstAbstractExpression expression;

  AstExpressionStatement() {}

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  void setExpression(AstAbstractExpression expression) {
    this.expression = expression;
  }

  AstAbstractExpression getExpression() {
    return expression;
  }
}
