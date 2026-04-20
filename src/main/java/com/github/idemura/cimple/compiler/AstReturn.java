package com.github.idemura.cimple.compiler;

public class AstReturn extends AstStatement {
  private AstExpression expression;

  AstReturn(Location location, AstExpression expression) {
    super(location);
    this.expression = expression;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  AstExpression getExpression() {
    return expression;
  }

  void setExpression(AstExpression expression) {
    this.expression = expression;
  }
}
