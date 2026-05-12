package com.github.idemura.cimple.compiler.ast;

public final class AstDelete extends AstStatement {
  private AstExpressionRoot expression;

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
