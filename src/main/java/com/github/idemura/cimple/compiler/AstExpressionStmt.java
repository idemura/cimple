package com.github.idemura.cimple.compiler;

// Assignment expression
public class AstExpressionStmt extends AstStatement {
  private final AstExpression expression;

  AstExpressionStmt(Location location, AstExpression expression) {
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
}
