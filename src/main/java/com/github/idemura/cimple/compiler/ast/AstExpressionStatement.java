package com.github.idemura.cimple.compiler.ast;

public final class AstExpressionStatement extends AstStatement {
  private AstExpressionRoot expression;

  public static AstExpressionStatement of(AstExpression expression) {
    var stmt = new AstExpressionStatement();
    stmt.expression(new AstExpressionRoot(expression));
    return stmt;
  }

  public AstExpressionStatement() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public AstExpressionRoot expression() {
    return expression;
  }

  public void expression(AstExpressionRoot expression) {
    this.expression = expression;
  }
}
