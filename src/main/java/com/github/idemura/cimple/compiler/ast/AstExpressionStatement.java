package com.github.idemura.cimple.compiler.ast;

public final class AstExpressionStatement extends AstStatement {
  private AstExpression expression;

  public static AstExpressionStatement of(AstExpression expression) {
    var stmt = new AstExpressionStatement();
    stmt.setExpression(expression);
    return stmt;
  }

  public AstExpressionStatement() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public AstExpression getExpression() {
    return expression;
  }

  public void setExpression(AstExpression expression) {
    this.expression = expression;
  }
}
