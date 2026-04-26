package com.github.idemura.cimple.compiler;

public class AstDefer extends AstAbstractStatement {
  private AstAbstractExpression expression;

  AstDefer() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  AstAbstractExpression getExpression() {
    return expression;
  }

  void setExpression(AstAbstractExpression expression) {
    this.expression = expression;
  }
}
