package com.github.idemura.cimple.compiler.ast;

public class AstDefer extends AstStatement {
  private AstExpression expression;

  public AstDefer() {}

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
