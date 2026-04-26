package com.github.idemura.cimple.compiler;

public class AstReturn extends AstAbstractStatement {
  private AstAbstractExpression expression;

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  AstAbstractExpression getExpression() {
    return expression;
  }

  void setExpression(AstAbstractExpression expression) {
    this.expression = expression;
  }
}
