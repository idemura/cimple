package com.github.idemura.cimple.compiler.ast;

public final class AstExpressionStatement extends AstStatement {
  private AstExpressionHolder expression;

  public static AstExpressionStatement of(AstExpression expression) {
    var stmt = new AstExpressionStatement();
    stmt.expression(new AstExpressionHolder(expression));
    return stmt;
  }

  public AstExpressionStatement() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public AstExpressionHolder expression() {
    return expression;
  }

  public void expression(AstExpressionHolder expression) {
    this.expression = expression;
  }
}
